package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import slick.driver.PostgresDriver.api._
import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.dispatch.ExecutionContexts
import akka.io.Udp.SO.Broadcast
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import pl.dyskobol.model.{File, FileProperties, FlowElements}
import pl.dyskobol.prototype.persist.Main.mimetypes
import pl.dyskobol.prototype.plugins.filters
import pl.dyskobol.prototype.persist.Main.{db, mimetypes}
import pl.dyskobol.prototype.persist.Tables.{MimeTypes, Properties}

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random



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
    val properties = TableQuery[Properties]
    val schema = properties.schema ++ mimetypes.schema

    val db = Database.forURL("jdbc:postgresql://localhost/postgres?user=postgres&password=postgres")
    db.run(schema.create)

    for( (_, (file, props)) <- processed ) {
      println("-----------------------------------------------------")
      if( file.path contains "@" ) {
        println("                                                                              FROM ZIP")
      }
      println(f"${file.path}/${file.name}, ${file.mime}")
      println(props)

      var i = Math.abs(Random.nextInt)
      try{
        Await.result(db.run(
          mimetypes += (i, f"${file.mime}", f"${file.path}/${file.name}")), Duration.apply(15, "seconds"))
      } //finally db.close


      for((propertyName, stringValue) <- props.numberValues ){
        try{
            Await.result(db.run(DBIO.seq(
              properties += (i,f"${propertyName}",f"${stringValue.toString}"))),Duration.apply(15, "seconds"))
        }
      }
      for((propertyName, stringValue) <- props.stringValues ){
        try{
          Await.result(db.run(DBIO.seq(
            properties += (i,f"${propertyName}",f"${stringValue.toString}"))),Duration.apply(15, "seconds"))
        }
      }
      for((propertyName, stringValue) <- props.dateValues ){
        try{
          Await.result(db.run(DBIO.seq(
            properties += (i,f"${propertyName}",f"${stringValue.toString}"))),Duration.apply(15, "seconds"))
        }
      }
      for((propertyName, stringValue) <- props.byteValues ){
        try{
          Await.result(db.run(DBIO.seq(
            properties += (i,f"${propertyName}",f"${stringValue.toString}"))),Duration.apply(15, "seconds"))
        } finally db.close
      }

    }
    system.terminate()
  })

}