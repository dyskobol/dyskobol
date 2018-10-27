package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import akka.dispatch.ExecutionContexts
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream._

import scala.concurrent.Future
import akka.event.Logging
import pl.dyskobol.prototype.plugins.metrics.TimeMonitor




object DyskobolSystem extends App {


  def run(graph: ActorRef  =>GraphDSL.Builder[Future[Done]] ⇒ SinkShape[Any] => ClosedShape)(onComplete: => Unit): Unit = {

    implicit val system = ActorSystem("dyskobol")
    val log = Logging.getLogger(system, this)
    val timeMonitor = system.actorOf(Props[TimeMonitor], "time-monitor")
    val decider: Supervision.Decider = {
      e: Throwable => {
        log.error(e.getMessage)
        Supervision.Resume
      }
    }

    implicit val executionContext = ExecutionContexts.global()

    implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

    val sink = Sink.ignore



    RunnableGraph
      .fromGraph(GraphDSL.create(sink) {graph(timeMonitor)})
      .run()
      .onComplete(_ => {
        system.terminate()
        timeMonitor ! "stop"
        onComplete})
  }

  def readConfig(path: String): Config = ConfigFactory.parseFile(new java.io.File(path))
}