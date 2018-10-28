//package pl.dyskobol.prototype.stages
//
//import akka.NotUsed
//import akka.actor.ActorSystem
//import akka.dispatch.ExecutionContexts
//import akka.stream.{ActorMaterializer, ClosedShape}
//import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Source}
//import org.scalatest.FunSuite
//import pl.dyskobol.model.FlowElements
//import akka.stream.scaladsl.GraphDSL.Implicits._
//import pl.dyskobol.prototype.stages
//
//class sourceTest extends FunSuite {
//
//
//  test("testFileSource") {
//    implicit val system = ActorSystem("dyskobol")
//    implicit val materializer = ActorMaterializer()
//    implicit val executionContext = ExecutionContexts.global()
//    var got = 0
//
//    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
//      val source      = stages.VfsFileSource("./core/res/test.iso")
//      val sink        = stages.Sink()
//      val printer     = builder.add(Flow[FlowElements].map(fp => {
//        println(fp._1.name)
//        got += 1
//        fp
//      }))
//      val taker = builder.add(Flow[FlowElements].take(5))
//
//
//      source ~> taker ~> printer ~> sink
//
//
//
//      ClosedShape
//    }).run()
//
//
//    Thread.sleep(5000)
//    assert(got == 5)
//
//  }
//
//  test("simpleIntSource") {
//    implicit val system = ActorSystem("dyskobol")
//    implicit val materializer = ActorMaterializer()
//    implicit val executionContext = ExecutionContexts.global()
//    var got = 0
//
//    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
//      val source      = builder.add(Source(1 to 10))
//      val sink        = stages.Sink()
//      val printer     = builder.add(Flow[Int].map(v => {
//        println(v)
//        got += 1
//        v
//      }))
//      val taker = builder.add(Flow[Int].take(5))
//
//
//      source ~> taker ~> printer ~> sink
//
//
//
//      ClosedShape
//    }).run()
//
//
//    Thread.sleep(5000)
//    assert(got == 5)
//
//  }
//
//}
