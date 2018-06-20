package pl.dyskobol.prototype.plugins

import java.io.InputStream

import akka.stream.scaladsl.Flow
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{ParseContext, Parser}
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.parser.microsoft.OfficeParser
import org.apache.tika.parser.odf.OpenDocumentParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.parser.xml.XMLParser
import org.apache.tika.sax.WriteOutContentHandler
import pl.dyskobol.model.{FileProperties, FlowElements}
import pl.dyskobol.prototype.persistance.DB
import pl.dyskobol.prototype.plugins.document.{DocumentContentExtract, DocumentMetadataExtract}
import pl.dyskobol.prototype.plugins.document.filters.mimesIn
import pl.dyskobol.prototype.stages.ForEach

package object db {
  object flows {
    def SaveFile(bufferSize: Int = 100)(implicit db: DB) = Flow.fromGraph(new SaveFileGraph(bufferSize)(db))
  }
}