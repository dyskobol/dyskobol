package pl.dyskobol.prototype.stages

import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.Supervision.{Restart, Resume, Stop}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream._
import pl.dyskobol.model.FlowElements


class ForEach[A](f: (FlowElements) => Unit) extends GraphStage[FlowShape[FlowElements,FlowElements]] {

  val in: Inlet[FlowElements] = Inlet[FlowElements]("ForEach.in")
  val out: Outlet[FlowElements] = Outlet[FlowElements]("ForEach.out")

  override val shape: FlowShape[(FlowElements), (FlowElements)] = FlowShape.of(in, out)


  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      def decider = attr.get[SupervisionStrategy].map(_.decider).getOrElse(Supervision.resumingDecider)
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val (file , fileProp) = grab(in)

          try{
            f(file, fileProp)
          } catch {
            case e: Throwable =>
              decider(e) match {
                case Stop => failStage(e)
                case _ => pull(in)
            }

          }
          push(out, (file , fileProp))
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}