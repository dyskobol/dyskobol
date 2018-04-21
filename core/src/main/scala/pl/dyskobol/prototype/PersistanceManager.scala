package pl.dyskobol.prototype

import akka.actor.SupervisorStrategy.{Decider, Stop}
import akka.actor.{Actor, ActorContext, ActorLogging, ActorRef, ChildRestartStats, SupervisorStrategy}
import pl.dyskobol.model.File
import pl.dyskobol.prototype.plugin.FileProperties

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class PersistanceManager extends Actor with ActorLogging {
  var nextId: Long = 1

  override def receive: Receive = {
    case file: File => {

     // file.id success nextId
      nextId += 1
     // log.info(file.toString)
    }
    case (props: FileProperties, file: File) =>
      if( file.id != 0 ) {
        log.info(s"${file.id}-----\n${props.toString}\n-----")
      } else {
        self ! (props, file)
      }
  }
}
