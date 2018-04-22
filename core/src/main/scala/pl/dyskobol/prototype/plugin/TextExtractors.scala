package pl.dyskobol.prototype.plugin

import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.sax.WriteOutContentHandler
import pl.dyskobol.prototype.plugin.Plugin.Processor

object TextExtractors {
  val htmlExtract : Processor = (file)=>{
    val props = new FileProperties

    val handler = new WriteOutContentHandler(-1)
    val metadata = new Metadata()
    new HtmlParser().parse(file.createStream(), handler, metadata, new ParseContext())
    metadata.names().foreach((k) => {
      props.addProperty(k, metadata.get(k))
    })
    props.addProperty("content",handler.toString.replaceAll(" +|\t+", " ").replaceAll("\n+", "\n"))
    (props, Nil)
  }


}
