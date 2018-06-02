package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.actor.{ActorSystem, Props}
import akka.dispatch.ExecutionContexts
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink}
import akka.stream.{ActorMaterializer, ClosedShape, Materializer}
import pl.dyskobol.prototype.plugin.factories.Linkers
import akka.stream.scaladsl.GraphDSL.Implicits._
import pl.dyskobol.prototype.plugin.{DocsExtractor, FileMetaExtract, ImageMetaExtract, Plugin}


object Main extends App {
  implicit val system = ActorSystem("dyskobol")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = ExecutionContexts.global();
  implicit val imgsTypesToProess = Seq()
  implicit val docsTyps = Seq()


  RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    val source    = builder.add(stages.FileSource("./core/res/test.iso"))
    val broadCast = builder.add(Linkers.broadcast(3))
    val balancer  = builder.add(Linkers.balance(2))
    val imageFlow = builder.add(new ImageMetaExtract(imgsTypesToProess,"@databaseURl",(20,20)).flow())
    val metaFlow  = builder.add(new FileMetaExtract("@filemetaDatabaseUrl").flow())
    val docFlow   = builder.add(new DocsExtractor(docsTyps, "@content", "@meta").flow())
    val end       = builder.add(Sink.ignore)
              broadCast ~>  balancer ~> imageFlow ~> end
    source ~> broadCast ~>  balancer ~> metaFlow ~> end
              broadCast ~>  balancer ~> docFlow ~> end

    ClosedShape
  }).run()

}