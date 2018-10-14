package pl.dyskobol.prototype.plugins


import java.io.{BufferedWriter, FileWriter}

import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink}
import pl.dyskobol.model.{File, FileProperties, FlowElements}
import pl.dyskobol.persistance.{CommandHandler, SaveFiles, SaveProps}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


package object db {
  object flows {
    def PersistFiles(bufferSize: Int = 100)(implicit commandHandler: CommandHandler) =
        Flow[FlowElements].buffer(bufferSize, OverflowStrategy.backpressure)
          .grouped(bufferSize).mapConcat(fe => {
          val listOfFlowElements = fe.toList
          Await.result( commandHandler.persist(SaveFiles(listOfFlowElements.map(_._1))), Duration.Inf )
          listOfFlowElements
        }).async

    def PersistProps(bufferSize: Int = 100)(implicit commandHandler: CommandHandler) =
      Flow[FlowElements].buffer(bufferSize, OverflowStrategy.backpressure)
        .grouped(bufferSize).mapConcat(fe => {
        val listOfFlowElements = fe.toList
        val toPersist = SaveProps(listOfFlowElements.map(fe => (fe._1.id, fe._2)))
        commandHandler.persist(toPersist)
        listOfFlowElements
      }).async
  }
}
package object dummyDb{
  object flows{
    def PersistFiles(bufferSize : Int = 100) =
    Flow[FlowElements].buffer(bufferSize, OverflowStrategy.backpressure)
      .grouped(bufferSize).mapConcat(fe => {
      val listOfFlowElements = fe.toList
      //listOfFlowElements.foreach(println)
      listOfFlowElements
    }).async

    def PersistProps(bufferSize: Int = 100) =
      Flow[FlowElements].buffer(bufferSize, OverflowStrategy.backpressure)
        .grouped(bufferSize).mapConcat(fe => {
        val listOfFlowElements = fe.toList
        listOfFlowElements.foreach(
          x => writeToFile(x)
          )
        listOfFlowElements
      }).async

    def writeToFile(list : (File, FileProperties)): Unit ={
      val file = new java.io.File("log_file")
      val bw = new FileWriter("log_file", true)
      bw.write(list.toString() + "\n\n")
      bw.close()
    }
    def clearLogFile() : Unit ={
      import java.io.PrintWriter
      val file = new java.io.File("log_file")
      val writer = new PrintWriter(file)
      writer.print("")

    }
  }
}