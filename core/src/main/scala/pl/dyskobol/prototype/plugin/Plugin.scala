package pl.dyskobol.prototype.plugin

import akka.NotUsed
import akka.stream.{ FlowShape, Graph}
import akka.stream.scaladsl.GraphDSL
import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.plugin.Plugin.FlowElements
import pl.dyskobol.prototype.plugin.factories._
import akka.stream.scaladsl.GraphDSL.Implicits._
import pl.dyskobol.prototype.plugin.persisters.Persisters
trait Plugin {
  def name: String
  def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed]
}

object Plugin {
  type FlowElements = (File, FileProperties)
  type ProcessingResult = (FileProperties, Iterable[File])
}

class FileMetaExtract(val databaseUrl:String, val excludedMimeTypes: Seq[String] = Seq()) extends Plugin {
  override def name: String = "file metadata extractor"
  override def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed] = {
    GraphDSL.create() { implicit builder =>
      val guard       = builder.add(Filters.mimesNotIn(excludedMimeTypes))
      val extractor   = builder.add(Meta.fileMeta(true))
      val persister   = builder.add(Persisters.oraclePersister(databaseUrl))

      guard ~> extractor ~> persister
      FlowShape(guard.in, persister.out)
    }.named(name)

  }
}

class ImageMetaExtract(val mimeTypes: Seq[String] = Seq(), val databaseUrl:String, val thumbnailSizeWH: (Int, Int)= (25,25)) extends Plugin {
  override def name: String = "image metadata extractor"
  override def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed] = {

    GraphDSL.create() { implicit builder =>
      val guard             = builder.add(Filters.mimesIn(mimeTypes))
      val metaExtractor     = builder.add(Meta.imageMeta())
      val miniatureCreator  = builder.add(Content.thumbNail(thumbnailSizeWH._1, thumbnailSizeWH._2))
      val persister         = builder.add(Persisters.oraclePersister(databaseUrl))
      guard ~> metaExtractor ~> miniatureCreator ~> persister
      FlowShape(guard.in, persister.out)
    }.named(name)

  }
}
class DocsExtractor(val mimeTypes: Seq[String] = Seq(), val contentDatabaseUrl:String, val metaDatabaseUrl:String) extends Plugin {
  override def name: String = "image metadata extractor"
  override def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed] = {


    GraphDSL.create() { implicit builder =>
      val guard               = builder.add(Filters.mimesIn(mimeTypes))
      val broadcast           = builder.add(Linkers.broadcast(6))
      val xmlExtractor        = builder.add(Meta.xmlExtract())
      val pdfExtractor        = builder.add(Meta.PDFExtract())
      val htmlExtractor       = builder.add(Meta.htmlExtract())
      val msExtractor         = builder.add(Meta.msOfficeExtract())
      val openOfficeExtractor = builder.add(Meta.openOfficeExtract())
      val txtExtractor        = builder.add(Meta.txtExtract())
      val xmlFilter           = builder.add(Meta.xmlExtract())
      val pdfFilter           = builder.add(Meta.PDFExtract())
      val htmlFilter          = builder.add(Meta.htmlExtract())
      val msFilter            = builder.add(Meta.msOfficeExtract())
      val openOfficeFilter    = builder.add(Meta.openOfficeExtract())
      val txtExtractorFilter  = builder.add(Meta.txtExtract())
      val txtContentExtractor = builder.add(Content.txtExtract())
      val pdfContentExtractor = builder.add(Content.PDFExtract())
      val contentDatabase     = builder.add(Persisters.oraclePersister(contentDatabaseUrl))
      val metaDatabase        = builder.add(Persisters.oraclePersister(metaDatabaseUrl))
      val metaJoiner          = builder.add(Linkers.merge(4))
      val contentJoiner       = builder.add(Linkers.merge(2))
      val joiner              = builder.add(Linkers.merge(2))




      guard ~>  broadcast ~> xmlFilter          ~> xmlExtractor                                 ~> metaJoiner       ~> metaDatabase        ~> joiner
                broadcast ~> pdfFilter          ~> pdfExtractor         ~> pdfContentExtractor  ~> contentJoiner    ~> contentDatabase     ~> joiner
                broadcast ~> htmlFilter         ~> htmlExtractor                                ~> metaJoiner
                broadcast ~> msFilter           ~> msExtractor                                  ~> metaJoiner
                broadcast ~> openOfficeFilter   ~> openOfficeExtractor                          ~> metaJoiner
                broadcast ~> txtExtractorFilter ~> txtExtractor         ~> txtContentExtractor  ~> contentJoiner


      FlowShape(guard.in, joiner.out)
    }.named(name)

  }
}