package pl.dyskobol.examples


import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, GraphDSL, Merge}
import com.google.inject.Guice
import com.typesafe.config.{Config, ConfigFactory}
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.plugins.metrics.Configure
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}

import scala.concurrent.Future


object MetricsApp extends App {

  if (args.length < 1) {
    println("No configuration file provided")
  } else {
    val config = readConfig(args(0))
    run(config)
  }

  def run(conf: Config, workers: Int = 4): Unit = {
    clearLogFile()
    val injector = Guice.createInjector(new DyskobolModule())
    val dyskobolSystem = injector.getInstance(classOf[DyskobolSystem])

    dyskobolSystem.run{ implicit processMonitor => implicit builder => sink =>

        processMonitor ! Configure(System.out)

        val source          = builder add stages.VfsFileSource(conf.getObject("dyskobol").toConfig.getString("imagePath"))

        val balancer = builder add Balance[FlowElements](workers, waitForAllDownstreams = true)
        val merge = builder add Merge[FlowElements](workers)
        val mimeResolver    = builder add plugins.filetype.flows.resolver
        val persistFiles = builder add plugins.dummyDb.flows.PersistFiles()
        val persistProps = builder add plugins.dummyDb.flows.PersistProps()
        val (flowCheckIn, flowCheckOut) = plugins.metrics.ProcessingTimeGateways("process.full")


        val worker = GraphDSL.create() {implicit builder => {

          val broadcast       = builder add stages.Broadcast(3)
          val fileMeta        = builder add plugins.file.flows.FileMetadataExtract(full = false)
          val imageProcessing = builder add plugins.image.flows.ImageMetaExtract("image/jpeg"::Nil)
          val docMeta         = builder add  plugins.document.flows.DocumentMetaDataExtract()
          val merge           = builder add  stages.Merge(3)
          val (docsCheckin, docsCheckOut) = plugins.metrics.ProcessingTimeGateways("process.docs")
          val (basicCheckIn, basicCheckOut) = plugins.metrics.ProcessingTimeGateways("process.basic")
          val (imageCheckIn, imageCheckOut) = plugins.metrics.ProcessingTimeGateways("process.image")



          broadcast ~> imageCheckIn~> imageProcessing~>imageCheckOut  ~> merge
          broadcast ~> docsCheckin~> docMeta ~> docsCheckOut         ~> merge
          broadcast ~> basicCheckIn~> fileMeta  ~> basicCheckOut     ~> merge

          FlowShape[FlowElements, FlowElements](broadcast.in, merge.out)
        }}

        source ~> flowCheckIn ~> mimeResolver ~> persistFiles ~> balancer
        for (_ ← 1 to workers) {
          balancer ~> worker.async ~> merge
        }
        merge ~> persistProps ~> flowCheckOut ~> sink


        ClosedShape
    } {

      println("COMPLETED")
    }
  }

  def readConfig(path: String): Config = ConfigFactory.parseFile(new java.io.File(path))
}