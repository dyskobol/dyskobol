package pl.dyskobol.prototype.plugin.factories

import akka.stream.scaladsl.{Balance, Broadcast, Merge}
import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.plugin.Plugin.FlowElements

object Linkers {
  def broadcast(outs: Int): Broadcast[FlowElements] = Broadcast[FlowElements](outs)
  def merge(ins:Int): Merge[FlowElements] = Merge[FlowElements](ins)
  def balance(to:Int): Balance[FlowElements] = Balance[FlowElements](to)
}
