package pl.dyskobol.prototype.plugins.image

import akka.NotUsed
import akka.stream.scaladsl.GraphDSL
import akka.stream.{FlowShape, Graph}
import pl.dyskobol.model.FlowElements
import akka.stream.scaladsl.GraphDSL.Implicits._

import pl.dyskobol.prototype.plugins.plugin

class ImageMetaExtract(val mimeTypes: Seq[String] = Seq(), val extractThumbnails: Boolean = false, val thumbnailSizeWH: (Int, Int) = (25, 25)) extends plugin {
  override def name: String = "image metadata extractor"

  override def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed] = {

    GraphDSL.create() { implicit builder =>
      val guard = builder add filters.mimesIn(mimeTypes)
      val metaExtractor = builder add foreaches.imageMeta()

      if( extractThumbnails ) {
        val miniatureCreator = builder add foreaches.resize(thumbnailSizeWH._1, thumbnailSizeWH._2, "thumbnail")

        guard ~> metaExtractor ~> miniatureCreator
        FlowShape(guard.in, miniatureCreator.out)
      } else {
        guard ~> metaExtractor
        FlowShape(guard.in, metaExtractor.out)
      }
    }.named(name)

  }
}
