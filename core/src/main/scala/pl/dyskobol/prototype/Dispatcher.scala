package pl.dyskobol.prototype

import akka.actor.{Actor, ActorLogging}
import org.apache.tika.Tika
import pl.dyskobol.model.File
import pl.dyskobol.prototype.workers.AbstractFileWorker

import scala.collection.immutable.Queue

class Dispatcher extends Actor with ActorLogging{
  val actorsPerFileType = 5
  //val workersMap = scala.collection.mutable.Map[String, Queue[AbstractFileWorker]]
  val tika = new Tika



  override def receive = {
    case file: File =>
      tika.detect(file.createStream(),file.name) match {

        case "application/pdf" => {
          log.info("dispatching\t<pdf>\t{} ", file.name)

        }
        case "text/plain" => {
          log.info("dispatching\t<text/plain>\t{}", file.name)
        }
        case message: String => log.info("dispatching other {} <{}>: ", file.name, message)
        case _ => log.info("Unknown")
      }

  }

  def dispatch() ={}


}
