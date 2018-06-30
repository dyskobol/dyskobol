package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import akka.dispatch.ExecutionContexts
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import pl.dyskobol.persistance.{CommandHandler, Persist}
import pl.dyskobol.prototype.persistance.DB
import pl.dyskobol.prototype.stages.GeneratedFilesBuffer


object Main extends App {
  implicit val system = ActorSystem("dyskobol")
  val conf = ConfigFactory.parseFile(new java.io.File(args.apply(0)))
  implicit val dbs = Map("relational" -> new DB(
    conf.getObjectList("dyskobol.dbs.postgres").get(0).toConfig.getString("host"),
    conf.getObjectList("dyskobol.dbs.postgres").get(0).toConfig.getString("dbName"),
    conf.getObjectList("dyskobol.dbs.postgres").get(0).toConfig.getString("username"),
    conf.getObjectList("dyskobol.dbs.postgres").get(0).toConfig.getString("password")))
  implicit val actionRespository = pl.dyskobol.persistance.basicRepository
  implicit val commandHandler = new CommandHandler()

  val decider: Supervision.Decider = {
    case _: Throwable => Supervision.Resume
  }

  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))
  implicit val executionContext = ExecutionContexts.global()


  val sink = Sink.ignore
  RunnableGraph.fromGraph(GraphDSL.create(sink) { implicit builder => sink =>
    implicit val bufferedGenerated = new GeneratedFilesBuffer
    val source          = builder add  stages.FileSource(conf.getObject("dyskobol").toConfig.getString("imagePath"))
    val broadcast       = builder add stages.Broadcast(4)
    val fileMeta        = builder add plugins.file.flows.FileMetadataExtract(full = false)
    val imageProcessing = builder add plugins.image.flows.ImageMetaExtract("image/jpeg"::Nil)
    val docMeta         = builder add  plugins.document.flows.DocumentMetaDataExtract().withAttributes(ActorAttributes.supervisionStrategy(decider))
    val merge           = builder add  stages.Merge(3)
    val persistFiles    = builder add plugins.db.flows.PersistFiles()
    val persistProps    = builder add plugins.db.flows.PersistProps()
    val mimeResolver    = builder add plugins.filetype.flows.resolver
    val unzip           = builder add plugins.unzip.filesGenerators.unzip


    source ~> mimeResolver ~> persistFiles ~>  broadcast ~> imageProcessing  ~> merge ~> persistProps ~> sink
                                               broadcast ~> docMeta          ~> merge
                                               broadcast ~> fileMeta         ~> merge
                                      unzip <~ broadcast


    ClosedShape
  }).run()(materializer).onComplete(_ => {
    println("COMPLETED")
    system.terminate()
  })

}