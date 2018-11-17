package pl.dyskobol.examples

import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.google.inject.Guice
import com.typesafe.config.Config
import pl.dyskobol.prototype.customstages.GeneratedFilesBuffer
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}


object ImageApp extends Process {

  def run(conf: Config): Unit = {
    clearLogFile()
    val injector = Guice.createInjector(new DyskobolModule())
    val dyskobolSystem = injector.getInstance(classOf[DyskobolSystem])

    dyskobolSystem.run{ implicit processMonitor => implicit builder => sink =>

    implicit val bufferedGenerated = new GeneratedFilesBuffer
      val source          = builder add  stages.VfsFileSource(conf.getObject("dyskobol.process").toConfig.getString("imagePath"))
      val imageProcessing = builder add plugins.image.flows.ImageMetaExtract("image/jpeg"::"image/tiff"::Nil)
      val mimeResolver    = builder add plugins.filetype.flows.resolver
      val persistFiles = builder add plugins.dummyDb.flows.PersistFiles()
      val persistProps = builder add plugins.dummyDb.flows.PersistProps()



      source ~> mimeResolver ~> persistFiles ~> imageProcessing ~> persistProps ~> sink


      ClosedShape
    } {
      println("COMPLETED")
    }
  }

}