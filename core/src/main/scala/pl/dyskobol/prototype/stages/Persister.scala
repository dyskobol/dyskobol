package pl.dyskobol.prototype.stages

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.plugin.Plugin.FlowElements

class Persister[A](val databaseUrl:String) extends GraphStage[FlowShape[FlowElements, FlowElements]] {

  val in: Inlet[(File, FileProperties)] = Inlet[(File, FileProperties)]("Persistencer.in")
  val out: Outlet[(File, FileProperties)] = Outlet[(File, FileProperties)]("Persistencer.out")

  override val shape: FlowShape[(File, FileProperties), (File, FileProperties)] = FlowShape.of(in, out)
  var nextId:Long =0
  override def createLogic(attr: Attributes): GraphStageLogic =

  new GraphStageLogic(shape) {


    override def preStart(): Unit = {
      Thread.sleep(1000)
      //simulating connection initialzaiton
      nextId = (Math.random() * 1000).toLong
    }

    setHandler(in, new InHandler {

      def merge(fileProp: FileProperties): Unit = {
        //simulation merge
        Thread.sleep(100)
      }

      def persist(fileProp: FileProperties): Unit = {
        //simulation persist
        Thread.sleep(100)
        fileProp._fileId = Some(nextId)
        nextId += 1
        fileProp.clear()
      }

      override def onPush(): Unit = {
        val (file, fileProp) = grab(in)
        fileProp._fileId match {
          case Some(_) => merge(fileProp)
          case None => persist(fileProp)
        }

        push(out, (file, fileProp))
      }
    })

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        pull(in)
      }
    })
  }
}