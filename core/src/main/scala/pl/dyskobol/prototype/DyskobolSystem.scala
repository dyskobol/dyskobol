package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.actor.{ActorSystem, Props}
import pl.dyskobol.prototype.plugin.{FileProperties, Plugin, SimplePlugin, SimpleProcessors}
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.ImageMetadata.Item
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata


object Main extends App {
  val simplePlugin = new SimplePlugin("file")
  simplePlugin.addProcessor("text/plain", SimpleProcessors.first200)
 // simplePlugin.addProcessor("image/jpeg", SimpleProcessors.imageMeta)
 // simplePlugin.addProcessor("image/jpg", SimpleProcessors.imageMeta)
 // simplePlugin.addProcessor("image/tiff", SimpleProcessors.imageMeta)
  simplePlugin.addProcessor("application/zip", SimpleProcessors.zipExtract)
 // val s = SimpleProcessors.first200
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
