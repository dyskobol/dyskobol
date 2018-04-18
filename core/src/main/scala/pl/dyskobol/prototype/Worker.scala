package pl.dyskobol.prototype

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import pl.dyskobol.model.File
import pl.dyskobol.prototype.plugin.Plugin.Processor
import pl.dyskobol.prototype.plugin.{FileProperties, Plugin}

class Worker(val processor: Processor, val dispatcher: ActorRef, val persistanceManager: ActorRef) extends Actor with ActorLogging {
  override def receive =  {
    case file: File => {
      val processed = processor(file)
      val fileProperties = processed._1
      val additionalFiles = processed._2
      additionalFiles.foreach(dispatcher ! _)
      persistanceManager ! (fileProperties, file)
    }
    case "stop" => {
      self ! PoisonPill
    }
  }
}
