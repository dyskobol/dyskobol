package pl.dyskobol.prototype.plugins

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL
import javax.imageio.ImageIO
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.IImageMetadata.IImageMetadataItem
import org.apache.commons.imaging.common.ImageMetadata
import org.apache.commons.imaging.common.ImageMetadata.Item
import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.stages
import pl.dyskobol.prototype.stages.ForEach

package object image {

  object flows {
    def ImageMetaExtract(mimeTypes: Seq[String] = Seq(), extractThumbnails: Boolean = false, thumbnailSizeWH: (Int, Int) = (25, 25)) =
      new ImageMetaExtract(mimeTypes, extractThumbnails, thumbnailSizeWH).flow()
  }

  object filters extends filters {
    //filters for images

  }

  object foreaches extends foreaches {

    def imageMeta(onlyKeys: Seq[String] = Nil): ForEach[(File, FileProperties)] = {
      onlyKeys match {
        case Nil =>
          //extract all metadata
          ForEach((pair: FlowElements) => {
            val (file, prop) = pair
            val bytes = Array.ofDim[Byte](file.size.toInt)
            file.createStream().read(bytes)
            Option(Imaging.getMetadata(bytes)) foreach (_.getItems.forEach(item => {
              prop.addProperty(item.asInstanceOf[Item].getKeyword, item.asInstanceOf[Item].getText)
            }))
          })

        case seq: Seq[String] =>
          //extract only defined in param
          ForEach((pair: (FlowElements)) => {
            val (file, prop) = pair
            val bytes = Array.ofDim[Byte](file.size.toInt)
            file.createStream().read(bytes)
            Option(Imaging.getMetadata(bytes)) foreach (_.asInstanceOf[ImageMetadata].getItems
              .stream
              .filter(item => seq.contains(item.asInstanceOf[Item].getKeyword))
              .forEach(item => {
                prop.addProperty(item.asInstanceOf[Item].getKeyword, item.asInstanceOf[Item].getText)
              }))
          })

      }
    }

    def resize(width:Int, height:Int, key:String = "thumbnail", format:String = "jpg")(implicit builder: GraphDSL.Builder[NotUsed]) =
      ForEach((pair: (File, FileProperties)) => {
        val img: BufferedImage = ImageIO.read(pair._1.createStream())
        val dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = dimg.createGraphics
        g2d.drawImage(img, 0, 0, null)
        val baos = new ByteArrayOutputStream()
        ImageIO.write(dimg, format, baos)
        pair._2.addProperty(key, baos.toByteArray)
      })
  }
}

