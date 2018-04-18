package pl.dyskobol.prototype

import akka.actor.SupervisorStrategy.{Decider, Stop}
import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ChildRestartStats, PoisonPill, Props, SupervisorStrategy}

import scala.reflect._
import org.apache.tika.Tika
import pl.dyskobol.model.File
import pl.dyskobol.prototype.plugin.Plugin

import scala.collection.mutable
import scala.collection.mutable.Queue

class Dispatcher(val plugins: List[Plugin], val persistanceManager: ActorRef, onEnd: => Unit) extends Actor with ActorLogging{
  val workersPerPlugin = 5

  private def areAllDifferentNames(): Boolean = {
    val pluginNames = plugins.map(_.name)
    pluginNames.toSet.size == pluginNames.size
  }

  if( !areAllDifferentNames() ) {
    throw new IllegalArgumentException("Duplicated plugins found")
  }

  private def createPluginDispatcher(plugin: Plugin): ActorRef =
    context.actorOf(Props(new PluginLevelDispatcher(plugin, self, persistanceManager, workersPerPlugin)))

  private def mimesToPlugins(): (Seq[ActorRef], Map[String, Set[ActorRef]]) = {
    val dispatcherMap = mutable.HashMap.empty[String, mutable.Set[ActorRef]]
    val dispatcherList = mutable.ListBuffer.empty[ActorRef]
    for (plugin <- plugins) {
      val pluginDispatcher = createPluginDispatcher(plugin)
      dispatcherList += pluginDispatcher
      for (mime <- plugin.supportedFiles) {
        if( dispatcherMap.contains(mime) ) {
          dispatcherMap(mime) += pluginDispatcher
        } else {
          dispatcherMap(mime) = mutable.Set(pluginDispatcher)
        }
      }
    }

    (dispatcherList.toList, dispatcherMap.map(kv => (kv._1, kv._2.toSet)).toMap.withDefaultValue(Set()))
  }

  val (dispatcherList, dispatcherMap) = mimesToPlugins()

  var terminated = 0

  override def receive = {
    case file: File => {
      if( dispatcherMap(file.mime).isEmpty ) {
        log.info(s"Mime ${file.mime} doesn't have a processor");
      }
      persistanceManager ! file
      dispatcherMap(file.mime).foreach(_ ! file)
    }
    case "stop" => dispatcherList.foreach(_ ! "stop")
  }

  override def supervisorStrategy: SupervisorStrategy = {
    new StopAfterChildren ((_) => {
      terminated += 1
      if( terminated == dispatcherList.length ) {
        log.info("DISPATCHER TERMINATED")
        self ! PoisonPill
        persistanceManager ! PoisonPill
      }
    })
  }

  override def postStop(): Unit = onEnd
}

class PluginLevelDispatcher(val plugin: Plugin, val dispatcher: ActorRef, val persistanceManager: ActorRef, val workersCount: Int) extends Actor with ActorLogging {
  if( workersCount < 1 ) throw new IllegalArgumentException("There has to be at least one worker")

  private val actors = mutable.Queue[ActorRef]()

  for( i <- 1 to workersCount) addWorker()

  override def receive: Receive = {
    case file: File => {
      val worker = actors.dequeue()
      worker ! file
      actors += worker
    }
    case "stop" => actors.foreach(_ ! "stop")
  }

  def addWorker(): Unit = {
    actors += context.actorOf(Props(new Worker(plugin.processor(), dispatcher, persistanceManager)))
  }

  override def supervisorStrategy: SupervisorStrategy = {
    val ref = self
    new StopAfterChildren ((child) => {
      actors.dequeueFirst(_ == child)
      if( actors.isEmpty ) {
        log.info("STOPPED")
        ref ! PoisonPill
      }
    })
  }
}

class StopAfterChildren(onStop: ActorRef => Unit) extends SupervisorStrategy {
  override def decider: Decider =  {
    case _ => Stop
  }

  override def handleChildTerminated(context: ActorContext, child: ActorRef, children: Iterable[ActorRef]): Unit = {
    onStop(child)
  }

  override def processFailure(context: ActorContext, restart: Boolean, child: ActorRef, cause: Throwable, stats: ChildRestartStats, children: Iterable[ChildRestartStats]): Unit = {
    cause.printStackTrace()
  }
}
