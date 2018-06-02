package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.actor.{ActorSystem, Props}
import akka.dispatch.ExecutionContexts
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink}
import pl.dyskobol.prototype.plugin._
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.ImageMetadata.Item
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata
import akka.stream.{ActorMaterializer, ClosedShape, Materializer}
import pl.dyskobol.prototype.plugin.factories.Linkers
import akka.stream.scaladsl.GraphDSL.Implicits._


object Main extends App {
  implicit val system = ActorSystem("dyskobol")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = ExecutionContexts.global();

  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val source    = builder.add(stages.FileSource("./core/res/test.iso"))
    val broadCast  = builder.add(Linkers.broadcast(3))

              broadCast ~>
    source ~> broadCast ~>
              broadCast ~>

    ClosedShape
  })


  stages.FileSource("./core/res/test.iso").map(f => {
    println( f"${f.path}/${f.name} : ${f.mime}")
    f
  }).runWith(Sink.ignore).onComplete { _ =>  system.terminate() }
}