package pl.dyskobol.prototype

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source}
import pl.dyskobol.model.{File, FlowElements}

import scala.concurrent.duration.Duration
package object stages {
  def VfsFileSource(path: String)(implicit bufferedGenerated: GeneratedFilesBuffer = null)
  = Source.fromGraph( new VfsFileSource(path, Duration(1, TimeUnit.SECONDS))( Option(bufferedGenerated) )).async

  def ForEach(f: FlowElements => Unit): Flow[FlowElements, FlowElements, NotUsed] = Flow[FlowElements].map(fe => {f(fe); fe})
  def Filter(f: FlowElements => Boolean) = Flow[FlowElements].filter(f)
  def Broadcast(out: Int) = akka.stream.scaladsl.Broadcast[FlowElements](out)
  def Merge(ins: Int) = akka.stream.scaladsl.Merge[FlowElements](ins)
  def Sink() = akka.stream.scaladsl.Sink.ignore
  type FilesGenerator = File => Iterator[File]
}
