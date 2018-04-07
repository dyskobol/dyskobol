package pl.dyskobol.prototype

import akka.actor.{Actor, ActorLogging, ActorRef}
import pl.dyskobol.model.File
import pl.dyskobol.prototype.Dyskobol.img
import simple.Library

class DiskReader(val path: String, val dispatcher: ActorRef)  extends Actor with ActorLogging {

  override def preStart() {
    log.info("Starting WorkerActor instance hashcode # {}", this.hashCode())
    log.info("opening a filesystem ")
    val filesystem = Library.openFsNat(Library.openImgNat(path))
    log.info("opened")
  }
  override def postStop() {
    log.info("Stopping WorkerActor instance hashcode # {}", this.hashCode())
  }


  override def receive ={

    case "start" => log.info("starting")

    case _ => log.info("dispatching oth")

  }

}
