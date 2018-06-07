package pl.dyskobol.prototype.plugins.document

import akka.NotUsed
import akka.stream.scaladsl.GraphDSL
import akka.stream.{FlowShape, Graph}
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.plugins.plugin
import pl.dyskobol.prototype.stages
import akka.stream.scaladsl.GraphDSL.Implicits._

class DocumentMetadataExtract extends plugin {
  override def name: String = "image metadata extractor"

  override def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      val broadcast           = builder add stages.Broadcast(6)
      val xmlExtractor        = builder add foreaches.xmlMeta()
      val pdfExtractor        = builder add foreaches.pdfMetat()
      val htmlExtractor       = builder add foreaches.htmlMeta()
      val msExtractor         = builder add foreaches.msOfficeMeta()
      val openOfficeExtractor = builder add foreaches.openMeta()
      val txtExtractor        = builder add foreaches.txtMeta()
      val xmlFilter           = builder add filters.isXml
      val pdfFilter           = builder add filters.isPdf
      val htmlFilter          = builder add filters.isHtml
      val msFilter            = builder add filters.isMsOffice
      val openOfficeFilter    = builder add filters.isOpenOffice
      val txtExtractorFilter  = builder add filters.isTxt
      val merge               = builder add stages.Merge(6)


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