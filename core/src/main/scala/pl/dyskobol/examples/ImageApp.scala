package pl.dyskobol.examples

import akka.stream._
import akka.stream.scaladsl.{Balance, GraphDSL, Merge}
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.google.inject.Guice
import com.typesafe.config.Config
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.customstages.GeneratedFilesBuffer
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}


object ImageApp extends Process {

  def run(conf: Config): Unit = {
    clearLogFile()
    val injector = Guice.createInjector(new DyskobolModule())
    val dyskobolSystem = injector.getInstance(classOf[DyskobolSystem])
    val workers = conf.getObject("dyskobol.process").toConfig.getInt("workers")

    dyskobolSystem.run { implicit processMonitor => implicit builder => sink =>

      implicit val bufferedGenerated = new GeneratedFilesBuffer
      val source = builder add stages.VfsFileSource(conf.getObject("dyskobol.process").toConfig.getString("imagePath"))
      val balancer = builder add Balance[FlowElements](workers, waitForAllDownstreams = true)
      val merge = builder add Merge[FlowElements](workers)

      val worker = GraphDSL.create() {implicit builder => {
        val imageProcessing = builder add plugins.image.flows.ImageMetaExtract("image/jpeg" :: "image/tiff" :: Nil)
        val mimeResolver = builder add plugins.filetype.flows.resolver
        val persistFiles = builder add plugins.dummyDb.flows.PersistFiles()
        val persistProps = builder add plugins.dummyDb.flows.PersistProps()

        mimeResolver ~> persistFiles ~> imageProcessing ~> persistProps
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