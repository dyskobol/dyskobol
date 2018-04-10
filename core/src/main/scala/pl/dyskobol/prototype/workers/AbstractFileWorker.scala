package pl.dyskobol.prototype.workers

import akka.actor.{Actor, ActorLogging, ActorRef}
import pl.dyskobol.model.File

abstract class AbstractFileWorker extends Actor with ActorLogging{
  override def receive ={
    case file: File if canWork => work(file)
    case file: File if !canWork => sender() ! ("cannot", file)
  }

  def canWork: Boolean

  def work(file: File): Unit //or some work Results
}
