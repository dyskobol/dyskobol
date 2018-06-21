package pl.dyskobol.prototype.plugins.file

import akka.NotUsed
import akka.stream.{FlowShape, Graph}
import akka.stream.scaladsl.GraphDSL
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.plugins.plugin
import akka.stream.scaladsl.GraphDSL.Implicits._


class FileMetadataExtract(full: Boolean = true) extends plugin {
  override def name: String = "file metadata extractor"
  override def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed] = foreaches.fileMeta(full).named(name)
}