package pl.dyskobol.examples

import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.typesafe.config.Config
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.stages.GeneratedFilesBuffer
import pl.dyskobol.prototype.{DyskobolSystem, plugins, stages}



object UnzipExampleApp extends App {
  if (args.length < 1) {
    println("No configuration file provided")
  } else {
    val config = DyskobolSystem.readConfig(args(0))
    run(config)
  }

  def run(conf: Config): Unit = {
    clearLogFile()

    DyskobolSystem.run{implicit builder => sink =>
      implicit val bufferedGenerated = new GeneratedFilesBuffer
      val source          = builder add  stages.VfsFileSource(conf.getObject("dyskobol").toConfig.getString("imagePath"))
      val broadcast       = builder add stages.Broadcast(4)
      val fileMeta        = builder add plugins.file.flows.FileMetadataExtract(full = false)
      val imageProcessing = builder add plugins.image.flows.ImageMetaExtract("image/jpeg"::Nil)
      val docMeta         = builder add  plugins.document.flows.DocumentMetaDataExtract()
      val merge           = builder add  stages.Merge(3)

      val mimeResolver    = builder add plugins.filetype.flows.resolver
      val unzip           = builder add plugins.unzip.filesGenerators.unzip
      val persistFiles = builder add plugins.dummyDb.flows.PersistFiles()
      val persistProps = builder add plugins.dummyDb.flows.PersistProps()


      source ~> mimeResolver ~> persistFiles ~>  broadcast ~> imageProcessing  ~> merge ~> persistProps ~> sink
                                                 broadcast ~> docMeta          ~> merge
                                                 broadcast ~> fileMeta         ~> merge
                                                 broadcast ~> unzip

      ClosedShape
    } {
      println("COMPLETED")
    }
  }
}