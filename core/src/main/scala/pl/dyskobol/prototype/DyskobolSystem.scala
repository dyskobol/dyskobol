package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.dispatch.ExecutionContexts
import akka.io.Udp.SO.Broadcast
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import pl.dyskobol.model.{File, FileProperties, FlowElements}
import pl.dyskobol.persistance.{CommandHandler, Persist}
import pl.dyskobol.prototype.persistance.DB
import pl.dyskobol.prototype.plugins.filters
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration


object Main extends App {
  implicit val system = ActorSystem("dyskobol")
  implicit val dbs = Map("relational" -> new DB("postgres", "postgres", "postgres"))
  implicit val actionRespository = pl.dyskobol.persistance.basicRepository
  implicit val commandHandler = new CommandHandler()

  val decider: Supervision.Decider = {
    case e => {
      e.printStackTrace()
      Supervision.Resume
    }
  }
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))
  implicit val executionContext = ExecutionContexts.global()

  // Since we merge flows that may produce the same files we need to make sure we don't print one twice
  // This is not an issue in final project - we'll use DB there
  val processed = mutable.Map[String, FlowElements]()
  val sink = Sink.foreach[FlowElements](f => {
    val (file, props) = f
    processed(file.path + file.name) = f
  })

  RunnableGraph.fromGraph(GraphDSL.create(sink) { implicit builder => sink =>
    val source      = builder add  stages.FileSource("./core/res/test.iso", plugins.unzip.filesGenerators.unzip)
    val broadcast   = builder add stages.Broadcast(3)
    val fileMeta    = builder add plugins.file.flows.FileMetadataExtract(full = false)
    val imageProcessing = builder add plugins.image.flows.ImageMetaExtract("image/jpeg"::Nil)
    val docMeta     = builder add  plugins.document.flows.DocumentMetaDataExtract().withAttributes(ActorAttributes.supervisionStrategy(decider))
    val merge       = builder add  stages.Merge(3)
    val persist         = builder add plugins.db.flows.SaveFile(100)


    source ~> persist ~> broadcast ~> imageProcessing  ~> merge ~> sink
                         broadcast ~> docMeta          ~> merge
                         broadcast ~> fileMeta         ~> merge


    ClosedShape
  }).run()(materializer).onComplete(_ => {
    for( (_, (file, props)) <- processed ) {
//      println("-----------------------------------------------------")
      if( file.path contains "@" ) {
        println("                                                                              FROM ZIP")
      }
//      println(f"${file.path}/${file.name}, ${file.mime}")
//      println(props)
    }

    commandHandler.persist(processed)
    system.terminate()
  })

}