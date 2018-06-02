package pl.dyskobol.prototype.plugin.factories

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, InputStream}
import java.lang.reflect.Field

import javax.imageio.ImageIO
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.common.ImageMetadata.Item
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.parser.{ParseContext, Parser}
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.parser.xml.XMLParser
import org.apache.tika.sax.WriteOutContentHandler
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight
import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.plugin.Plugin.FlowElements
import pl.dyskobol.prototype.plugin.factories.Content.extract
import pl.dyskobol.prototype.plugin.factories.TextExtractors.log
import pl.dyskobol.prototype.stages.ForEach


object Meta {
  def fileMeta(full: Boolean): ForEach[FlowElements] = {
    if (full) {
      new ForEach((pair: (File, FileProperties)) => {
        val (file, prop) = pair
        file.getClass.getDeclaredFields.foreach((f: Field) => {
          val value = f.get(file).toString
          if (value forall Character.isDigit)
            prop.addProperty(f.getName, BigDecimal(value))
          else
            prop.addProperty(f.getName, value)
        }
        )
      })
    } else {
      new ForEach((pair: (File, FileProperties)) => {
        val (file, prop) = pair
        prop.addProperty("size", file.size)
        prop.addProperty("path", file.path)
        prop.addProperty("name", file.name)
        prop.addProperty("mtype", file.mime())

      })
    }
  }

  def imageMeta(): ForEach[FlowElements] = new ForEach((pair: (File, FileProperties)) => {
    val (file, prop) = pair
    val bytes = Array.ofDim[Byte](file.size.toInt)
    file.createStream().read(bytes)

    Option(Imaging.getMetadata(bytes)) foreach (_.getItems.forEach((item) => {
      item.asInstanceOf[Item].getKeyword
      item.asInstanceOf[Item].getText
      prop.addProperty(item.asInstanceOf[Item].getKeyword, item.asInstanceOf[Item].getText)
    }))

  })


  def htmlExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new HtmlParser())

  def txtExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new TXTParser()())

  def PDFExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new PDFParser()())

  def xmlExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new XMLParser()())

  def msOfficeExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new OfficeParser()())

  def openOfficeExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new OpenDocumentParser()())

  private def extract(parser: Parser, inputStream: InputStream, props: FileProperties) = {
    val handler = new WriteOutContentHandler(-1)
    val metadata = new Metadata()
    parser.parse(inputStream, handler, metadata, new ParseContext())
    metadata.names().foreach((k) => {
      props.addProperty(k, metadata.get(k))
    })
    log.info(s"${props.toString}\n-----")

  }

}

object Content {
  def thumbNail(width: Int, height: Int): ForEach[FlowElements] = new ForEach((pair: (File, FileProperties)) => {
    val img: BufferedImage = ImageIO.read(pair._1.createStream())
    val dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val g2d = dimg.createGraphics
    g2d.drawImage(img, 0, 0, null)
    val baos = new ByteArrayOutputStream()
    ImageIO.write(dimg, "As,", baos)
    pair._2.addProperty("thumbnail", baos.toByteArray)

  })

  private def extract(parser: Parser, inputStream: InputStream, props: FileProperties) = {
    val handler = new WriteOutContentHandler(-1)
    val metadata = new Metadata()
    parser.parse(inputStream, handler, metadata, new ParseContext())
    props.addProperty("content", handler.toString.replaceAll(" +|\t+", " ").replaceAll("\n+", "\n"))
    log.info(s"${props.toString}\n-----")

  }

  def htmlExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new HtmlParser())

  def txtExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new TXTParser()())

  def PDFExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new PDFParser()())

  def xmlExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new XMLParser()())

  def msOfficeExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new OfficeParser()())

  def openOfficeExtract(): ForEach[FlowElements] = Helper.extractSkeleton(extract, new OpenDocumentParser()())
}

object Helper {
  def extractSkeleton(extracor: (Parser, InputStream, FileProperties) => Unit, parser: Parser): ForEach[FlowElements] = new ForEach((pair: (FlowElements)) => {
    val (file, prop) = pair
    extracor(parser, file.createStream(), prop)
  })
}




