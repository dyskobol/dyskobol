package pl.dyskobol.prototype.plugins.db

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import pl.dyskobol.model.FlowElements
import pl.dyskobol.persistance.CommandHandler
import pl.dyskobol.prototype.persistance.DB

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class SaveFileGraph(val bufferSize: Int = 100)(implicit commandHandler: CommandHandler) extends GraphStage[FlowShape[FlowElements, FlowElements]] {

  val in = Inlet[FlowElements]("Save.in")
  val out = Outlet[FlowElements]("Save.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic = {
    var buffered: List[FlowElements] = Nil
    var persisted: List[FlowElements] = Nil

    def bufferFull = buffered.size >= bufferSize

    def persist(): Future[Any] = commandHandler.persist(persisted.map(_._1))

    var downstreamWaiting = false

    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          var elem = grab(in)
          if (bufferFull && persisted.isEmpty) {
            persisted = buffered
            buffered = elem :: Nil
            Await.result(persist(), Duration.Inf)
            if (downstreamWaiting) {
              downstreamWaiting = false
              push(out, persisted.head)
              persisted = persisted.tail
              pull(in)
            }
          } else {
            buffered = elem +: buffered
            if (!bufferFull) {
              pull(in)
            } else
            if( downstreamWaiting ) {
              downstreamWaiting = false
              persisted = persisted ++ buffered
              buffered = Nil
              Await.result(persist(), Duration.Inf)
              push(out, persisted.head)
              persisted = persisted.tail
              pull(in)
            }
          }
        }

        override def onUpstreamFinish(): Unit = {
          var toEmit = persisted
          persisted = buffered
          Await.result(persist(), Duration.Inf)
          toEmit = persisted ++ toEmit
          if (toEmit.nonEmpty) {
            emitMultiple(out, toEmit)
          }

          completeStage()
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if (persisted.nonEmpty) {
            push(out, persisted.head)
            persisted = persisted.tail
          } else {
            downstreamWaiting = true
            pull(in)
          }
        }
      })
    }
  }
}