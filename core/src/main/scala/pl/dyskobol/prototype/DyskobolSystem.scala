package pl.dyskobol.prototype

import java.util.concurrent.CountDownLatch

import akka.actor.{ActorSystem, Props}
import pl.dyskobol.prototype.plugin._
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.ImageMetadata.Item
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata


object Main extends App {
  val simplePlugin = new SimplePlugin("file")
  val extractPlugin = new SimplePlugin("extract")


  simplePlugin.addProcessor("text/plain", SimpleProcessors.first200)
    .addProcessor("image/jpeg", SimpleProcessors.imageMeta)
    .addProcessor("image/jpg", SimpleProcessors.imageMeta)
    .addProcessor("image/tiff", SimpleProcessors.imageMeta)
    .addProcessor("application/zip", SimpleProcessors.zipExtract)
  extractPlugin //.addProcessor("text/html", TextExtractors.htmlExtract)

  List("application/msword",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-excel",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.ms-powerpoint",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
  ).foreach((mime) => {extractPlugin.addProcessor(mime, TextExtractors.msOfficeExtract)})

  List("application/vnd.oasis.opendocument.text",
    "application/vnd.oasis.opendocument.text-template",
    "application/vnd.oasis.opendocument.text-web",
    "application/vnd.oasis.opendocument.text-master",
    "application/vnd.oasis.opendocument.graphics 	",
    "application/vnd.oasis.opendocument.graphics-template",
    "application/vnd.oasis.opendocument.presentation",
    "application/vnd.oasis.opendocument.presentation-template",
    "application/vnd.oasis.opendocument.spreadsheet",
    "application/vnd.oasis.opendocument.spreadsheet-template",
    "application/vnd.oasis.opendocument.chart",
    "application/vnd.oasis.opendocument.formula",
    "application/vnd.oasis.opendocument.database",
    "application/vnd.oasis.opendocument.image",
    "application/vnd.openofficeorg.extension"
  ).foreach((mime) => {extractPlugin.addProcessor(mime, TextExtractors.openOfficeExtract)})


  extractPlugin
    .addProcessor("application/xml", TextExtractors.xmlExtract)
    .addProcessor("text/xml", TextExtractors.xmlExtract)
    .addProcessor("application/pdf", TextExtractors.PDFExtract)
    .addProcessor("text/plain", TextExtractors.txtExtract)

  DyskobolSystem.process("./core/res/test.iso", List(simplePlugin, extractPlugin))
}

object DyskobolSystem {
  def process(imagePath: String, plugins: List[Plugin]) {
    val dyskobolSystem = ActorSystem("dyskobol")
    val log = dyskobolSystem.log
    val l = new CountDownLatch(1)
    val persistanceManager = dyskobolSystem.actorOf(Props(new PersistanceManager))
    val dispatcher = dyskobolSystem.actorOf(Props(new Dispatcher(plugins, persistanceManager, {
      dyskobolSystem.terminate()
    })), name = "dispatcher")
    val reader = new DiskReader(imagePath, dispatcher)
    reader.digg()
    dispatcher ! "stop"
  }
}
