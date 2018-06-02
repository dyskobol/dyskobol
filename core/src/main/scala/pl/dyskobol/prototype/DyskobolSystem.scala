package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.actor.{ActorSystem, Props}
import akka.dispatch.ExecutionContexts
import akka.stream.scaladsl.Sink
import pl.dyskobol.prototype.plugin._
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.ImageMetadata.Item
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata
import akka.stream.{ActorMaterializer, Materializer}


object Main extends App {
  implicit val system = ActorSystem("dyskobol")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = ExecutionContexts.global();

  stages.FileSource("./core/res/test.iso").map(f => {
    println( f"${f.path}/${f.name} : ${f.mime}")
    f
  }).runWith(Sink.ignore).onComplete { _ =>  system.terminate() }
}