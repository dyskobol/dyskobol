package pl.dyskobol.prototype.plugins.metrics

import java.io.PrintStream

import akka.actor.Actor
import pl.dyskobol.prototype.plugins.metrics.{Configure, TimeMeasurement}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class TimeMonitor extends Actor{
  val times: mutable.Map[String, ListBuffer[Long]] = mutable.Map()
  var stream: PrintStream = System.out
  override def receive: Receive = {
    case Configure(stream) => {
      this.stream = stream
    }
    case TimeMeasurement(key, checkInMilis, checkOutMilis, flowElements) => {
      times.get(key)
        .map(measures => measures += (checkOutMilis - checkInMilis))
        .orElse(times.put(key, new ListBuffer() += checkOutMilis - checkInMilis))
    }

    case "stop" => {
      times.foreach(measures =>
        stream.println(s"${measures._1}: measures:${measures._2.length} mean: ${(measures._2.sum / measures._2.length) / Math.pow(10, 9)} s"))
    }
    case x => {}
  }
}