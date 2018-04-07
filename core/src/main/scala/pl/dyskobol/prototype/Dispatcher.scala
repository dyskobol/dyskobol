package pl.dyskobol.prototype

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.reflect._
import org.apache.tika.Tika
import pl.dyskobol.model.File
import pl.dyskobol.prototype.workers.application.{PdfFileWorker, TextFileWorker}

import scala.collection.mutable
import scala.collection.mutable.Queue

class Dispatcher extends Actor with ActorLogging{
  val actorsPerFileType = 5
  val workersMap = scala.collection.mutable.Map[String, mutable.Queue[ActorRef]]()
  val tika = new Tika



  override def receive = {
    case file: File =>
      tika.detect(file.createStream(),file.name) match {

        case "application/pdf" => {
          log.info("dispatching\t<pdf>\t{} ", file.name)
          dispatch(classTag[PdfFileWorker], file)

        }
        case "text/plain" => {
          log.info("dispatching\t<text/plain>\t{}", file.name)
          dispatch(classTag[TextFileWorker], file)
        }
        case message: String => log.info("dispatching other {} <{}>: ", file.name, message)
        case _ => log.info("Unknown")
      }

  }

  def dispatch(cls: ClassTag[_], file:File) ={
    val workersQueue = workersMap.getOrElse(cls.runtimeClass.getCanonicalName,
    {
      val newBornWorkers = Queue[ActorRef]()
      for (i: Int <- 1 to actorsPerFileType){
        val actor  = context.actorOf(
          Props( cls.runtimeClass),
          cls.runtimeClass.getCanonicalName.concat(i.toString))
        newBornWorkers.enqueue(actor)

      }
      workersMap.put(cls.runtimeClass.getCanonicalName, newBornWorkers)

      newBornWorkers
    })
    val worker : ActorRef = workersQueue.dequeue
    worker ! file
    workersQueue.enqueue(worker)

  }


}
