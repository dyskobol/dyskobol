package pl.dyskobol.prototype.stages

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import pl.dyskobol.model.FlowElements

class ForEach[A](f: (FlowElements) => Unit) extends GraphStage[FlowShape[FlowElements,FlowElements]] {

  val in: Inlet[FlowElements] = Inlet[FlowElements]("ForEach.in")
  val out: Outlet[FlowElements] = Outlet[FlowElements]("ForEach.out")

  override val shape: FlowShape[(FlowElements), (FlowElements)] = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {
      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val (file , fileProp) = grab(in)
          f(file, fileProp)
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