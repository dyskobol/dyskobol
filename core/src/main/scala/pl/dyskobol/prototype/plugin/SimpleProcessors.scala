package pl.dyskobol.prototype.plugin

import java.io._
import java.util.zip._

import akka.actor.ActorSystem
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.ImageMetadata.Item
import pl.dyskobol.prototype.plugin.Plugin.Processor
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import pl.dyskobol.model.{File, FileStream}

object SimpleProcessors {

  val system = ActorSystem("Dyskobol", ConfigFactory.load.getConfig("akka"))
  val log = Logging.getLogger(system, this)
  val first200 : Processor =
    (file) => {
      val props = new FileProperties
      val bytes = Array.ofDim[Byte](200)
      file.createStream().read(bytes)
      props.addProperty("first200", new String(bytes))
      (props, Nil)
    }


  val imageMeta : Processor =
    (file) =>{
      val props = new FileProperties
      val bytes = Array.ofDim[Byte](file.size.toInt)
      file.createStream().read(bytes)

        Option(Imaging.getMetadata(bytes)) foreach(_.getItems.forEach((item) => {
          item.asInstanceOf[Item].getKeyword
          item.asInstanceOf[Item].getText
          props.addProperty(item.asInstanceOf[Item].getKeyword ,item.asInstanceOf[Item].getText )
          log.info(s"File: ${file.path}, Props: ${props.toString}")
        }))

      (props, Nil)
    }

  val zipExtract : Processor = (file) => {
    val filesUnzipped = List()

    val props = new FileProperties
    val zis = new ZipInputStream(file.createStream())
    Stream.continually(zis.getNextEntry).takeWhile(_ != null).foreach { zipped =>
      props.addProperty("contains", zipped.getName)
      log.info(s"contains: ${zipped.getName}")


      var unzipped = new File(zipped.getName, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0) {
        override def createStream(): FileStream = {
          val is :FileStream = null

          is
        }
      }
      filesUnzipped.+:(unzipped)
    }

    (props, filesUnzipped)
    }






}
