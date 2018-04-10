package pl.dyskobol.prototype.plugin

import pl.dyskobol.model.File
import pl.dyskobol.prototype.plugin.Plugin.FileProcessor

trait Plugin {
  def name: String
  def supportedFiles: Iterable[String]
  def process(mime: String, file: File): FileProperties
}

class SimplePlugin(val name: String) extends Plugin {
  var mapping: Map[String, FileProcessor] = Map()

  override def supportedFiles: Iterable[String] = mapping.keys
  override def process(mime: String, file: File): FileProperties = {
    if( mapping contains mime) {
      return mapping(mime)(file)
    }
    throw new IllegalArgumentException(f"Mime ${mime} is not supported by plugin ${name}")
  }

  def addProcessor(mime: String, processor: FileProcessor): SimplePlugin = {
    mapping + (mime -> processor)
    this
  }
}


object Plugin {
  type FileProcessor = File=>FileProperties

  def plugin(name: String): SimplePluginBuilder1 = new SimplePluginBuilder1(new SimplePlugin(name))

  implicit def SimplePlugin(i: SimplePluginBuilder1) = i.plugin

  case class SimplePluginBuilder1(plugin: SimplePlugin) {
    def on(mime: String) = new SimplePluginBuilder2(Seq(mime), this)
    def on(mimes: Seq[String]) = new SimplePluginBuilder2(mimes, this)
  }
  case class SimplePluginBuilder2(mimes: Iterable[String], builder: SimplePluginBuilder1) {
    def perform(processor: FileProcessor): SimplePluginBuilder1 = {
      for( mime <- mimes ) {
        builder.plugin.addProcessor(mime, processor)
      }
      builder
    }
  }

  def test(): Seq[Plugin] = {
    Seq[SimplePlugin](
      plugin("test")
      on "dsad"
      perform ((x) => {
        new FileProperties(x)
      }),

      plugin("trolo")
      on Seq("xd", "2323")
      perform {(x) => new FileProperties(x)}
    )
  }
}