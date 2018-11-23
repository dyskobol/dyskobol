package pl.dyskobol.examples

import akka.stream._
import akka.stream.scaladsl.{Balance, GraphDSL, Merge}
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.google.inject.Guice
import com.typesafe.config.{Config, ConfigFactory}
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.customstages.GeneratedFilesBuffer
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}



object UnzipExampleApp extends Process {

  def run(conf: Config): Unit = {
    clearLogFile()
    val injector = Guice.createInjector(new DyskobolModule())
    val dyskobolSystem = injector.getInstance(classOf[DyskobolSystem])
    val workers = conf.getObject("dyskobol.process").toConfig.getInt("workers")

    dyskobolSystem.run{ implicit processMonitor => implicit builder => sink =>

    implicit val bufferedGenerated = new GeneratedFilesBuffer
      val source          = builder add  stages.VfsFileSource(conf.getObject("dyskobol.process").toConfig.getString("imagePath"))
      val balancer = builder add Balance[FlowElements](workers, waitForAllDownstreams = true)
      val merge = builder add Merge[FlowElements](workers)

      val worker = GraphDSL.create() {implicit builder => {
        val broadcast       = builder add stages.Broadcast(4)
        val fileMeta        = builder add plugins.file.flows.FileMetadataExtract(full = false)
        val imageProcessing = builder add plugins.image.flows.ImageMetaExtract("image/jpeg"::Nil)
        val docMeta         = builder add  plugins.document.flows.DocumentMetaDataExtract()
        val merge           = builder add  stages.Merge(3)

        val mimeResolver    = builder add plugins.filetype.flows.resolver
        val unzip           = builder add plugins.unzip.filesGenerators.unzip
        val persistFiles = builder add plugins.dummyDb.flows.PersistFiles()
        val persistProps = builder add plugins.dummyDb.flows.PersistProps()

        mimeResolver ~> persistFiles ~>  broadcast ~> imageProcessing  ~> merge ~> persistProps
                                         broadcast ~> docMeta          ~> merge
                                         broadcast ~> fileMeta         ~> merge
                                         broadcast ~> unzip
        FlowShape[FlowElements, FlowElements](mimeResolver.in, persistProps.out)
      }}

      source ~> balancer
      for( _ <- 1 to workers ) {
        balancer ~> worker.async ~> merge
      }
      merge ~> sink

      ClosedShape
    } {
      println("COMPLETED")
    }
  }

}