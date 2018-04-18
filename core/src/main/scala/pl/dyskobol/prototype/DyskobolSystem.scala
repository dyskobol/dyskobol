package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.actor.{ActorSystem, Props}
import pl.dyskobol.prototype.plugin.{FileProperties, Plugin, SimplePlugin}


object Main extends App {
  val simplePlugin = new SimplePlugin("file")
  simplePlugin.addProcessor("text/plain", (file) => {
    val props = new FileProperties
    val bytes = Array.ofDim[Byte](200)
    file.createStream().read(bytes)
    props.addProperty("first200", new String(bytes))
    (props, Nil)
  })
  print(simplePlugin.supportedFiles)

  DyskobolSystem.process( "./core/res/test.iso", List(simplePlugin) )
}

object DyskobolSystem {
  def process(imagePath: String, plugins: List[Plugin]) {
    val dyskobolSystem = ActorSystem("dyskobol")
    val log = dyskobolSystem.log
    val l = new CountDownLatch(1)
    val persistanceManager = dyskobolSystem.actorOf(Props(new PersistanceManager))
    val dispatcher = dyskobolSystem.actorOf(Props(new Dispatcher(plugins, persistanceManager, {dyskobolSystem.terminate()})), name = "dispatcher")
    val reader = new DiskReader(imagePath, dispatcher)
    reader.digg()
    dispatcher ! "stop"
  }
}
