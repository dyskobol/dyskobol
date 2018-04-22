package pl.dyskobol.prototype.plugin

import java.io.InputStream

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import org.apache.tika.metadata.{Metadata, TikaCoreProperties}
import org.apache.tika.parser.{ParseContext, Parser}
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.opendocument.OpenOfficeParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.parser.xml.XMLParser
import org.apache.tika.sax.WriteOutContentHandler
import pl.dyskobol.prototype.plugin.Plugin.Processor


object TextExtractors {
  val system = ActorSystem("Dyskobol", ConfigFactory.load.getConfig("akka"))
  val log = Logging.getLogger(system, this)

  private def extract(parser: Parser, inputStream :InputStream, props: FileProperties)={
    val handler = new WriteOutContentHandler(-1)
    val metadata = new Metadata()
    parser.parse(inputStream, handler, metadata, new ParseContext())
    metadata.names().foreach((k) => {
      props.addProperty(k, metadata.get(k))
    })

    props.addProperty("content",handler.toString.replaceAll(" +|\t+", " ").replaceAll("\n+", "\n"))
    log.info(s"${props.toString}\n-----")
    (metadata, handler)

  }
  val txtExtract : Processor = (file)=>{
    val props = new FileProperties
    extract(new TXTParser(), file.createStream(), props)
    (props, Nil)
  }
  val PDFExtract : Processor = (file)=>{
    val props = new FileProperties
    extract(new PDFParser(), file.createStream(), props)
    (props, Nil)
  }
  val htmlExtract : Processor = (file)=>{
    val props = new FileProperties
    extract(new HtmlParser(), file.createStream(), props)
    (props, Nil)
  }

  val xmlExtract : Processor = (file)=>{
    val props = new FileProperties
    extract(new XMLParser(), file.createStream(), props)
    (props, Nil)
  }

  val msOfficeExtract : Processor = (file)=>{
    val props = new FileProperties
    extract(new OfficeParser(), file.createStream(), props)
    (props, Nil)
  }

  val openOfficeExtract : Processor = (file)=>{
    val props = new FileProperties
    extract(new OpenDocumentParser(), file.createStream(), props)
    (props, Nil)
  }








}
