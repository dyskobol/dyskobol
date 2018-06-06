package pl.dyskobol.prototype.plugins.document

import akka.NotUsed
import akka.stream.scaladsl.GraphDSL
import akka.stream.{FlowShape, Graph}
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.plugins.plugin
import pl.dyskobol.prototype.stages
import akka.stream.scaladsl.GraphDSL.Implicits._

class DocumentContentExtract extends plugin {
  override def name: String = "image metadata extractor"

  override def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      val broadcast           = stages.Broadcast(6)
      val xmlExtractor        = foreaches.xmlContent()
      val pdfExtractor        = foreaches.pdfContent()
      val htmlExtractor       = foreaches.htmlContent()
      val msExtractor         = foreaches.msOfficeContent()
      val openOfficeExtractor = foreaches.openMeta()
      val txtExtractor        = foreaches.txtMeta()
      val xmlFilter           = filters.isXml
      val pdfFilter           = filters.isPdf
      val htmlFilter          = filters.isHtml
      val msFilter            = filters.isMsOffice
      val openOfficeFilter    = filters.isOpenOffice
      val txtExtractorFilter  = filters.isTxt
      val merge               = stages.Merge(6)


      broadcast ~> xmlFilter ~> xmlExtractor ~> merge
      broadcast ~> pdfFilter ~> pdfExtractor ~> merge
      broadcast ~> htmlFilter ~> htmlExtractor ~> merge
      broadcast ~> msFilter ~> msExtractor ~> merge
      broadcast ~> openOfficeFilter ~> openOfficeExtractor ~> merge
      broadcast ~> txtExtractorFilter ~> txtExtractor ~> merge


      FlowShape(broadcast.in, merge.out)
    }.named(name)
  }

}
