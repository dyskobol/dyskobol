package pl.dyskobol.examples

import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, GraphDSL, Merge}
import com.google.inject.Guice
import com.typesafe.config.Config
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.plugins.metrics.Configure
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}


object DocsApp extends Process {
  override def run(config: Config): Unit = {
    clearLogFile()
    val workers = config.getObject("dyskobol").toConfig.getInt("workers")
    val injector = Guice.createInjector(new DyskobolModule())
    val dyskobolSystem = injector.getInstance(classOf[DyskobolSystem])
    dyskobolSystem.run{ implicit processMonitor => implicit builder => sink =>
      processMonitor ! Configure(System.out)
      val source          = builder add stages.VfsFileSource(config.getObject("dyskobol").toConfig.getString("imagePath"))

      val balancer = builder add Balance[FlowElements](workers, waitForAllDownstreams = true)
      val merge = builder add Merge[FlowElements](workers)
      val mimeResolver    = builder add plugins.filetype.flows.resolver
      val persistFiles = builder add plugins.dummyDb.flows.PersistFiles()
      val persistProps = builder add plugins.dummyDb.flows.PersistProps()
      val (flowCheckIn, flowCheckOut) = plugins.metrics.ProcessingTimeGateways("process.full")

      val worker = GraphDSL.create() {implicit builder => {

        val docMeta         = builder add  plugins.document.flows.DocumentMetaDataExtract()
        val docContent      = builder add plugins.document.flows.DocumentContentExtract()
        val (docsCheckin, docsCheckOut) = plugins.metrics.ProcessingTimeGateways("process.docs")

        docsCheckin~> docMeta ~> docContent ~> docsCheckOut

        FlowShape[FlowElements, FlowElements](docsCheckin.in, docsCheckOut.out)
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
}