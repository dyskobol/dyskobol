package pl.dyskobol.prototype.plugin.factories


import akka.NotUsed
import akka.stream.scaladsl.Flow
import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.plugin.Plugin.FlowElements


object Filters {
  def sizeBetween(min: Long, max: Long): Flow[FlowElements, FlowElements, NotUsed] = Flow[FlowElements].filter((p: FlowElements) => (min to max) contains p._1.size)

  def mimesIn(mimeTypes: Seq[String]): Flow[FlowElements, FlowElements, NotUsed] = Flow[FlowElements].filter((p: FlowElements) => mimeTypes contains p._1.mime)

  def mimesNotIn(mimeTypes: Seq[String]): Flow[FlowElements, FlowElements, NotUsed] = Flow[FlowElements].filter((p: FlowElements) => !(mimeTypes contains p._1.mime))
  
  

}
