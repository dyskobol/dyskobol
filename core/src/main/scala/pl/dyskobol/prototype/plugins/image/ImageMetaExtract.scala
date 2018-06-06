package pl.dyskobol.prototype.plugins.image

import akka.NotUsed
import akka.stream.scaladsl.GraphDSL
import akka.stream.{FlowShape, Graph}
import pl.dyskobol.model.FlowElements
import akka.stream.scaladsl.GraphDSL.Implicits._

import pl.dyskobol.prototype.plugins.plugin

class ImageMetaExtract(val mimeTypes: Seq[String] = Seq(), val thumbnailSizeWH: (Int, Int) = (25, 25)) extends plugin {
  override def name: String = "image metadata extractor"

  override def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed] = {

    GraphDSL.create() { implicit builder =>
      val guard = filters.mimesIn(mimeTypes)
      val metaExtractor = foreaches.imageMeta()
      val miniatureCreator = foreaches.resize(thumbnailSizeWH._1, thumbnailSizeWH._2, "thumbnail")

      guard ~> metaExtractor ~> miniatureCreator


      FlowShape(guard.in, miniatureCreator.out)
    }.named(name)

  }
}
