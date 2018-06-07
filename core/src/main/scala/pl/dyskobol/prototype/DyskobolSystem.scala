package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.dispatch.ExecutionContexts
import akka.io.Udp.SO.Broadcast
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import pl.dyskobol.model.FlowElements



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

//
  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    val source      = builder add  stages.FileSource("./core/res/test.iso")
    val broadcast   = builder add stages.Broadcast(2)
    val fileMeta    = builder add plugins.file.flows.FileMetadataExtract(full = true)
    val docMeta     = builder add  plugins.document.flows.DocumentMetaDataExtract().withAttributes(ActorAttributes.supervisionStrategy(decider))
    val merge       = builder add  stages.Merge(2)
    val sink        = builder add Sink.foreach[FlowElements](f => {
      val (file, props) = f
      println("-----------------------------------------------------")
      println(f"${file.path}/${file.name}, ${file.mime}")
      println(props)
    })


    source ~> broadcast ~> fileMeta ~> merge
              broadcast ~> docMeta ~> merge ~> sink


    ClosedShape
  }).run()(materializer)

}