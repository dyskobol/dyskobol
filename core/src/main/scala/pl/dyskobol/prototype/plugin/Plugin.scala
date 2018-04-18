package pl.dyskobol.prototype.plugin

import pl.dyskobol.model.File

trait Plugin {
  def name: String
  def supportedFiles: Iterable[String]
  def processor(): Plugin.Processor
}

object Plugin {
  type ProcessingResult = (FileProperties, Iterable[File])
  type Processor = File => ProcessingResult
  implicit def filePropertiesToResult(props: FileProperties): ProcessingResult = { (props, Nil) }
}

class SimplePlugin(val name: String) extends Plugin {
  import Plugin._
  var mapping: Map[String, Processor] = Map()

  override def supportedFiles: Iterable[String] = mapping.keys

  override def processor(): Processor = process

  def addProcessor(mime: String, processor: Processor): SimplePlugin = {
    mapping += (mime -> processor)
    this
  }

  def process(file: File): ProcessingResult = {
    if( mapping contains file.mime) {
      return mapping(file.mime)(file)
    }
    throw new IllegalArgumentException(f"Mime ${file.mime} is not supported by plugin ${name}")
  }
}