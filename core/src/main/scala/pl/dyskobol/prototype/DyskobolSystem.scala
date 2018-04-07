package pl.dyskobol.prototype

import akka.actor.{ActorSystem, Props}


object DyskobolSystem extends App {
  val dyskobolSystem = ActorSystem("dyskobol")
  val log = dyskobolSystem.log

  val dispatcher = dyskobolSystem.actorOf(Props[Dispatcher], name = "dispatcher")
  val reader = dyskobolSystem.actorOf(Props(new DiskReader("/home/przemek/test.iso", dispatcher)), name = "reader")
  reader! "start"




}
