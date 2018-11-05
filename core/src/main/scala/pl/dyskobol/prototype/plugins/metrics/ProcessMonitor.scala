package pl.dyskobol.prototype.plugins.metrics

import java.io.PrintStream

import akka.actor.Actor
import me.tongfei.progressbar.{ProgressBar, ProgressBarBuilder, ProgressBarStyle}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ProcessMonitor extends Actor{
  val times: mutable.Map[String, ListBuffer[Long]] = mutable.Map()
  var totalSize:Long = 0
  var processedSize:Long = 0

  val pb  = new ProgressBarBuilder()
    .setInitialMax(100)
    .setTaskName("processing")
    .setUpdateIntervalMillis(1)
    .setStyle(ProgressBarStyle.UNICODE_BLOCK)
    .build()

  var stream: PrintStream = System.out

  def updateProgressBar(): Unit ={
    this.pb.stepTo((this.processedSize / Math.pow(10, 6)).toLong)
    this.pb.maxHint((this.totalSize / Math.pow(10, 6)).toLong)
  }

  override def receive: Receive = {
    case Configure(stream) => {
      this.stream = stream
    }
    case TimeMeasurement(key, checkInMilis, checkOutMilis, flowElements) => {
      times.get(key)
        .map(measures => measures += (checkOutMilis - checkInMilis))
        .orElse(times.put(key, new ListBuffer() += checkOutMilis - checkInMilis))
    }
    case AddToProcessing(size) =>{
      this.totalSize += size
      this.updateProgressBar()
    }

    case Processed(size) =>{
      this.processedSize += size
      this.updateProgressBar()
    }

    case "stop" => {

      this.pb.maxHint((this.totalSize / Math.pow(10, 6)).toLong)
      this.pb.stepTo((this.totalSize / Math.pow(10, 6)).toLong)
      this.pb.close()
      times.foreach(measures =>
        stream.println(s"${measures._1}: measures:${measures._2.length} mean: ${(measures._2.sum / measures._2.length) / Math.pow(10, 9)} s"))
    }
  }
}
