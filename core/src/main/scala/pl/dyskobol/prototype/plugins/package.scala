package pl.dyskobol.prototype

import akka.NotUsed
import pl.dyskobol.prototype.stages.Filter
import akka.stream.{FlowShape, Graph}
import akka.stream.scaladsl.{Flow, GraphDSL}
import pl.dyskobol.model.{File, FileProperties, FlowElements}

package object plugins {

  trait plugin {
    def name: String
    def flow(): Graph[FlowShape[(FlowElements), (FlowElements)], NotUsed]
  }

  class filters {

    def sizeBetween(min: Long, max: Long)(implicit builder: GraphDSL.Builder[NotUsed]) =
      Filter((p: FlowElements) => (min to max) contains p._1.size)

    def mimesIn(mimeTypes: Seq[String])(implicit builder: GraphDSL.Builder[NotUsed]) =
      Filter((p: FlowElements) => mimeTypes contains p._1.mime)

    def mimesNotIn(mimeTypes: Seq[String])(implicit builder: GraphDSL.Builder[NotUsed]) =
      Filter((p: FlowElements) => mimeTypes contains p._1.mime)
  }

  object filters {
    def apply: filters = new filters()
  }

  class foreaches{

  }
  object foreaches{
    def apply: foreaches = new foreaches()
  }



}
