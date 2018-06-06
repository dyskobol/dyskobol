package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.dispatch.ExecutionContexts
import akka.io.Udp.SO.Broadcast
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink}
import akka.stream.{ActorMaterializer, ClosedShape, Materializer}
import akka.stream.scaladsl.GraphDSL.Implicits._
import pl.dyskobol.model.FlowElements



object Main extends App {
  implicit val system = ActorSystem("dyskobol")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = ExecutionContexts.global()


  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    val source      = stages.FileSource("./core/res/test.iso")
    val broadcast   = stages.Broadcast(2)
    val fileMeta    = plugins.file.flows.FileMetadataExtract(true)
    val docMeta     = plugins.document.flows.DocumentMetaDataExtract()
    val merge       = stages.Merge(2)
    val sink        = stages.Sink()


    source ~> broadcast ~> fileMeta ~> merge
    broadcast ~> docMeta ~> merge
    merge ~> sink



    ClosedShape
  }).run()

}