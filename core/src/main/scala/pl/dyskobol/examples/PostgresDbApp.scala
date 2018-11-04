package pl.dyskobol.examples

import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.google.inject.Guice
import com.typesafe.config.{Config, ConfigFactory}
import pl.dyskobol.persistance.CommandHandler
import pl.dyskobol.prototype.persistance.DB
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}



object PostgresDbApp extends App {
  val a: Array[String] = args
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
      implicit val dbs = Map("relational" -> new DB(
        conf.getObjectList("dyskobol.dbs.postgres").get(0).toConfig.getString("host"),
        conf.getObjectList("dyskobol.dbs.postgres").get(0).toConfig.getString("dbName"),
        conf.getObjectList("dyskobol.dbs.postgres").get(0).toConfig.getString("username"),
        conf.getObjectList("dyskobol.dbs.postgres").get(0).toConfig.getString("password")))

      implicit val actionRespository = pl.dyskobol.persistance.basicRepository
      implicit val commandHandler = new CommandHandler()
      val source          = builder add  stages.VfsFileSource(conf.getObject("dyskobol").toConfig.getString("imagePath"))
      val broadcast       = builder add stages.Broadcast(3)
      val fileMeta        = builder add plugins.file.flows.FileMetadataExtract(full = false)
      val imageProcessing = builder add plugins.image.flows.ImageMetaExtract("image/jpeg"::Nil)
      val docMeta         = builder add  plugins.document.flows.DocumentMetaDataExtract()
      val merge           = builder add  stages.Merge(3)
      val mimeResolver    = builder add plugins.filetype.flows.resolver
      val persistFiles = builder add plugins.db.flows.PersistFiles()
      val persistProps = builder add plugins.db.flows.PersistProps()


      conf.getObject("dyskobol").toConfig.getString("flow")
                                                 broadcast ~> docMeta          ~> merge
                                                 broadcast ~> fileMeta         ~> merge

      ClosedShape
    } {
      println("COMPLETED")
    }
  }
  def readConfig(path: String): Config = ConfigFactory.parseFile(new java.io.File(path))

}