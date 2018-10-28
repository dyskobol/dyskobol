package pl.dyskobol.prototype.plugins

import akka.{Done, NotUsed}
import akka.actor._
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL}
import pl.dyskobol.model.{File, FileProperties, FlowElements}

package object metrics {
  case class Configure(stream: java.io.PrintStream)
  case class TimeMeasurement(key: String, checkInMilis: Long, checkOutMilis: Long, flowElements: FlowElements)
  case class GetResult(mean: Boolean, max: Boolean, min: Boolean)
  case class AddToProcessing(size: Long)
  case class Processed(size: Long)

  def checkInGateway(gatewaysKey: String): Flow[(File, FileProperties), (File, FileProperties), NotUsed] = Flow[FlowElements].map(fe => {
    fe._2.checkIn(gatewaysKey, System.nanoTime())
    fe}
  )


  def checkOutGateway(gatewaysKey: String, monitorActor: ActorRef): Flow[(File, FileProperties), (File, FileProperties), NotUsed] = Flow[FlowElements].map(fe => {
    val checkOut = System.nanoTime()
    fe._2.getCheckInTime(gatewaysKey).foreach(checkIn => monitorActor ! new TimeMeasurement(gatewaysKey, checkIn, checkOut, fe))
    fe}
  )

  def ProcessingTimeGateways(gatewaysKey: String)(implicit builder: GraphDSL.Builder[Any], processMonitor: ActorRef): (FlowShape[(File, FileProperties), (File, FileProperties)], FlowShape[(File, FileProperties), (File, FileProperties)]) =
    (builder.add(checkInGateway(gatewaysKey)), builder.add(checkOutGateway(gatewaysKey, processMonitor)))

}
