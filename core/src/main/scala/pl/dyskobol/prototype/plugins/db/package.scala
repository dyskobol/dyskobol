package pl.dyskobol.prototype.plugins


import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink}
import pl.dyskobol.model.FlowElements
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