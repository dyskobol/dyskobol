package pl.dyskobol.prototype.stages

import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.Supervision.{Restart, Resume, Stop}
import akka.stream.stage._
import akka.stream._
import pl.dyskobol.model.FlowElements


class ForEach[A](f: (FlowElements) => Unit) extends GraphStage[FlowShape[FlowElements,FlowElements]] {

  val in: Inlet[FlowElements] = Inlet[FlowElements]("ForEach.in")
  val out: Outlet[FlowElements] = Outlet[FlowElements]("ForEach.out")

  override val shape: FlowShape[(FlowElements), (FlowElements)] = FlowShape.of(in, out)


  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with StageLogging {
      var pulled = false

      def decider = attr.get[SupervisionStrategy].map(_.decider).getOrElse(Supervision.resumingDecider)
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          pulled = false

          val (file , fileProp) = grab(in)
          val clonnedFileProp = fileProp.deepClone()
          
          try{

            f(file, clonnedFileProp)
          } catch {
            case e: Throwable =>
              log.error(e.getMessage)
              decider(e) match {
                case Stop => failStage(e)
                case _ => {
                  pulled = true
                  pull(in)
                }
            }

          }
          push(out, (file , clonnedFileProp))
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if( !pulled ) {
            pull(in)
            pulled = true
          }
        }
      })
    }
}