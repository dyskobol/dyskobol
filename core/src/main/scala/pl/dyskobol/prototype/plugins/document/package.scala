package pl.dyskobol.prototype.plugins

import java.io.{File, FileInputStream, InputStream}

import akka.NotUsed
import akka.stream.Graph
import akka.stream.scaladsl.GraphDSL
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.tika.metadata.{MSOffice, Metadata}
import org.apache.tika.parser.{ParseContext, Parser}
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.parser.xml.XMLParser
import org.apache.tika.sax.WriteOutContentHandler
import pl.dyskobol.model.{FileProperties, FlowElements}
import pl.dyskobol.prototype.stages.ForEach

package object document {

  object flows {
    def DocumentContentExtract() = new DocumentContentExtract().flow()
    def DocumentMetaDataExtract() = new DocumentMetadataExtract().flow()
  }

  object filters extends filters {
    def isXml = mimesIn(supportedMimes.xml)

    def isTxt = mimesIn(supportedMimes.txt)

    def isPdf = mimesIn(supportedMimes.pdf)

    def isHtml = mimesIn(supportedMimes.html)

    def isMsOffice = mimesIn(supportedMimes.msOffice)

    def isOpenOffice = mimesIn(supportedMimes.openOffice)

    def isMsOfficeX = mimesIn(supportedMimes.msOfficeX)

  }

  object foreaches extends foreaches {
    def msofficeXContent() = extractCreator(metaExtract, new OOXMLParser)

    def htmlContent() =
      extractCreator(contentExtract, new HtmlParser())

    def txtContent() =
      extractCreator(contentExtract, new TXTParser())

    def xmlContent() =
      extractCreator(contentExtract, new XMLParser())

    def pdfContent() =
      extractCreator(contentExtract, new PDFParser())

    def msOfficeContent() =
      extractCreator(contentExtract, new OfficeParser())

    def openOffice() =
      extractCreator(contentExtract, new OpenDocumentParser())

    def htmlMeta() =
      extractCreator(metaExtract, new HtmlParser())

    def txtMeta() =
      extractCreator(metaExtract, new TXTParser())

    def xmlMeta() =
      extractCreator(metaExtract, new XMLParser())

    def pdfMetat() =
      extractCreator(metaExtract, new PDFParser())

    def msOfficeMeta() =
      extractCreator(metaExtract, new OfficeParser())

    def openMeta() =
      extractCreator(metaExtract, new OpenDocumentParser())

    def msOfficeXMeta() =
      extractCreator(metaExtract, new OOXMLParser())


    private def contentExtract(parser: Parser, inputStream: InputStream, props: FileProperties) = {
      val handler = new WriteOutContentHandler(-1)
      val metadata = new Metadata()
      parser.parse(inputStream, handler, metadata, new ParseContext())
      props.addProperty("content", handler.toString.replaceAll(" +|\t+", " ").replaceAll("\n+", "\n"))
    }

    private def metaExtract(parser: Parser, inputStream: InputStream, props: FileProperties) = {
      val handler = new WriteOutContentHandler(-1)
      val metadata = new Metadata()
      
        parser.parse(inputStream, handler, metadata, new ParseContext())
        metadata.names().foreach(k => {
          props.addProperty(k, metadata.get(k))
        })
    }

    private def extractCreator(extrFun: (Parser, InputStream, FileProperties) => Unit, parser: Parser) =
      ForEach((pair: (FlowElements)) => {
        val (file, prop) = pair

        extrFun(parser, file.createStream(), prop)
      })

  }

  object supportedMimes {
    val msOffice = List("application/msword",
      "application/vnd.ms-excel",
      "application/vnd.ms-excel",
      "application/vnd.ms-excel")

    val msOfficeX = List(
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      "application/vnd.ms-powerpoint"

    )
    val openOffice = List("application/vnd.oasis.opendocument.text",
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
    )
    val imgs = List("image/jpeg", "image/jpg", "image/tiff")
    val xml = List("application/xml", "text/xml")
    val html = List("application/html", "text/html")
    val pdf = List("application/pdf")
    val txt = List("application/txt")
  }

}
