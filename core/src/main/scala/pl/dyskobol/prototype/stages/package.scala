package pl.dyskobol.prototype

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source}
import pl.dyskobol.model.{File, FlowElements}
package object stages {
  def FileSource(path: String)(implicit builder: GraphDSL.Builder[NotUsed]) =builder.add(Source.fromGraph(new FileReaderGraph(path)))
  def ForEach(fe: ((FlowElements) => Unit))(implicit builder: GraphDSL.Builder[NotUsed]) =
    builder.add(new ForEach[FlowElements](fe))
  def Filter(fe: ((FlowElements) => Boolean))(implicit builder: GraphDSL.Builder[NotUsed]) =
    builder.add(Flow[FlowElements].filter(fe))
  def Broadcast(out: Int)(implicit builder: GraphDSL.Builder[NotUsed]) = builder.add(akka.stream.scaladsl.Broadcast[FlowElements](out))
  def Merge(ins: Int)(implicit builder: GraphDSL.Builder[NotUsed]) = builder.add(akka.stream.scaladsl.Merge[FlowElements](ins))
  def Sink()(implicit builder: GraphDSL.Builder[NotUsed]) = builder.add(akka.stream.scaladsl.Sink.ignore)

  val FileTypeResolver: Flow[FlowElements, FlowElements, NotUsed] = Flow[FlowElements].map(f=> {f._1.mime(); f})

}
