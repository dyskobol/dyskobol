package pl.dyskobol.prototype

import java.util.concurrent.TimeUnit

import akka.{Done, NotUsed}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.actor.ActorRef
import akka.stream.{FanOutShape, FlowShape, UniformFanOutShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Source}
import com.google.inject.Inject
import com.google.inject.name.Named
import pl.dyskobol.model.{File, FlowElements}
import pl.dyskobol.prototype.customstages.{GeneratedFilesBuffer, VFSFileSource}
import pl.dyskobol.prototype.plugins.document.{filters, foreaches}
import pl.dyskobol.prototype.plugins.metrics.{AddToProcessing, Processed}

import scala.concurrent.duration.Duration


object stages{

  @Inject()
  @Named("MonitorActor") val monitorActor:ActorRef = null

  def ForEach(f: FlowElements => Unit): Flow[FlowElements, FlowElements, NotUsed] = Flow[FlowElements].map(fe => {
    try {
      f(fe);
      fe
    } catch {
      case e: Exception => throw new DyskobolException(fe, e)
    }
  })
  def VfsFileSource(path: String)(implicit bufferedGenerated: GeneratedFilesBuffer = null)
  = Source.fromGraph( new VFSFileSource(path, Duration(1, TimeUnit.SECONDS))( monitorActor, Option(bufferedGenerated) )).async

  def Broadcast(out: Int)(implicit builder: GraphDSL.Builder[Any]) = {
    GraphDSL.create() { implicit builder =>
      val broadcast           = builder add akka.stream.scaladsl.Broadcast[FlowElements](out)
      val bf = builder.add(stages.ForEach((fe:FlowElements) => {
        monitorActor ! AddToProcessing(fe._1.size * (out - 1))
        fe;
      }))

      bf ~> broadcast

      UniformFanOutShape(bf.in, broadcast.outlets:_*)
  }}




  def Merge(ins: Int) = akka.stream.scaladsl.Merge[FlowElements](ins)
  def Sink() = akka.stream.scaladsl.Sink.ignore
  type FilesGenerator = File => Iterator[File]

  def Filter(f: FlowElements => Boolean) = Flow[FlowElements].filter(
    (fe:FlowElements) => {
      val res = f(fe)
      if(!res) monitorActor ! Processed(fe._1.size)
      res
    }
  )
}
