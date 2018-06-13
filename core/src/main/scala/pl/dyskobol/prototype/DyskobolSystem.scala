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
import pl.dyskobol.prototype.persist.Tables.{ByteValues, DateValues, MimeTypes, NumberValues, StringValues}

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

    val mimetypes = TableQuery[MimeTypes]
    val stringValues = TableQuery[StringValues]
    val byteValues = TableQuery[ByteValues]
    val numberValues = TableQuery[NumberValues]
    val dateValues = TableQuery[DateValues]

    val schema = stringValues.schema ++ numberValues.schema ++ dateValues.schema ++ byteValues.schema ++ mimetypes.schema

    val db = Database.forURL("jdbc:postgresql://localhost/postgres?user=postgres&password=postgres")
    Await.result(db.run(schema.create), Duration.Inf)

    var i = 0
    for( (_, (file, props)) <- processed ) {
      println("-----------------------------------------------------")
      if( file.path contains "@" ) {
        println("                                                                              FROM ZIP")
      }
      println(f"${file.path}/${file.name}, ${file.mime}")
      println(props)

      Await.result(db.run(
        mimetypes += (i, f"${file.mime}", f"${file.path}/${file.name}")), Duration.apply(15, "seconds"))

      for( (name, value) <- props.getAll() ) {
        value match {
          case v: String => {
            if(name != "name" && name != "mtype") // those are in "MIMETYPES" relation
              Await.result(db.run(DBIO.seq(
                stringValues += (i, name, value.asInstanceOf[String]))), Duration.apply(15, "seconds"))
          }
          case v: Array[Byte] => {
            Await.result(db.run(DBIO.seq(
              byteValues += (i, name, value.asInstanceOf[Blob]))), Duration.apply(15, "seconds"))
          }
          case v: java.util.Date => {
            Await.result(db.run(dateValues += (i, name, value.asInstanceOf[java.sql.Date])), Duration.apply(15, "seconds"))
          }
          case v: BigDecimal => {
            Await.result(db.run(numberValues += (i, name, value.asInstanceOf[BigDecimal])), Duration.apply(15, "seconds"))
          }
        }
      }

      i = i+1

    }
    db.close()
    system.terminate()
  })

}