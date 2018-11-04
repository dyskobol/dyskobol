package pl.dyskobol.prototype


import akka.Done
import akka.actor.ActorRef
import com.typesafe.config.{Config, ConfigFactory}
import akka.dispatch.ExecutionContexts
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink}
import akka.stream._

import scala.concurrent.Future
import akka.event.Logging
import pl.dyskobol.model.{File, FileProperties, FlowElements}
import pl.dyskobol.prototype.plugins.metrics.{ProcessMonitor, Processed}
import javax.inject.Named
import akka.actor.{ActorSystem, Props}

import com.google.inject.{AbstractModule, Inject}
import net.codingwell.scalaguice.ScalaModule

case class DyskobolException(fe: (File, FileProperties), e: Throwable) extends Exception


class DyskobolModule extends AbstractModule with ScalaModule{
  override def configure() : Unit = {
    val system = ActorSystem("Dyskobol")
    bind[ActorSystem].annotatedWithName("System").toInstance(system)
    bind[ActorRef].annotatedWithName("MonitorActor").toInstance(system.actorOf(Props(new ProcessMonitor()), "time-monitor"))
    bind[pl.dyskobol.prototype.stages.type].toInstance(pl.dyskobol.prototype.stages)
  }

}

class DyskobolSystem @Inject()(@Named("MonitorActor") val monitor: ActorRef, @Named("System") implicit val system: ActorSystem) extends App {


  def run(graph: ActorRef => GraphDSL.Builder[Future[Done]] ⇒ SinkShape[FlowElements] => ClosedShape)(onComplete: => Unit): Unit = {

    val log = Logging.getLogger(system, this)

    val decider: Supervision.Decider = {
      case DyskobolException(fe, e) =>
        log.error(e.getMessage)
        monitor ! Processed(fe._1.size)
        Supervision.Resume

      case e: Throwable =>
        log.error(e.getMessage)
        Supervision.Resume
    }

    implicit val executionContext = ExecutionContexts.global()

    implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

    val sink: Sink[(File, FileProperties), Future[Done]] = Sink.foreach((fe:FlowElements) => {monitor ! Processed(fe._1.size)})

    RunnableGraph
      .fromGraph(GraphDSL.create(sink) {graph(monitor)})
      .run()
      .onComplete(_ => {
        system.terminate()
        monitor ! "stop"
        onComplete})
  }
}