package pl.dyskobol.prototype

import java.sql.Blob
import java.util.Date
import java.sql.Date

import slick.driver.PostgresDriver.api._
import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.dispatch.ExecutionContexts
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import pl.dyskobol.model.{File, FileProperties, FlowElements}
import pl.dyskobol.prototype.persist.DB
import pl.dyskobol.prototype.persist.Tables.{ByteValues, DateValues, FileInfo, NumberValues, StringValues}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration



object Main extends App {
  implicit val system = ActorSystem("dyskobol")
  val decider: Supervision.Decider = {
    case e => {
      e.printStackTrace()
      Supervision.Resume
    }
  }
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))
  implicit val executionContext = ExecutionContexts.global()

  val db = new DB("dyskobol_example", "dyskobol", "dyskobol")

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


    source ~> broadcast ~> imageProcessing  ~> merge ~> sink
              broadcast ~> docMeta          ~> merge
              broadcast ~> fileMeta         ~> merge


    ClosedShape
  }).run()(materializer).onComplete(_ => {

    db.saveFiles(processed.values.map(_._1).toList).onComplete(f => if( f.isSuccess ) {
      db.saveProps {
        processed.values.map(fileAndProps => {
          val (file, props) = fileAndProps
          (file.id, props)
        })
      }.onComplete(_ => {
        db.close()
        system.terminate()
      })
    })
  })

}