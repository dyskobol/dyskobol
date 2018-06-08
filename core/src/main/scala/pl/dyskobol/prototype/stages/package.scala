package pl.dyskobol.prototype

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source}
import pl.dyskobol.model.{File, FlowElements}
package object stages {
  def FileSource(path: String, generator: FilesGenerator = (_) => Iterator.empty) = Source.fromGraph(new FileReaderGraph(path)(generator))

  type FilesGenerator = File => Iterator[File]

  def ForEach(fe: ((FlowElements) => Unit)) =
    new ForEach[FlowElements](fe)
  def Filter(fe: ((FlowElements) => Boolean)) =
    Flow[FlowElements].filter(fe)
  def Broadcast(out: Int) = akka.stream.scaladsl.Broadcast[FlowElements](out)
  def Merge(ins: Int) = akka.stream.scaladsl.Merge[FlowElements](ins)
  def Sink() = akka.stream.scaladsl.Sink.ignore

  val FileTypeResolver: Flow[FlowElements, FlowElements, NotUsed] = Flow[FlowElements].map(f=> {f._1.mime(); f})

}
