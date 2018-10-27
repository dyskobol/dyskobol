package pl.dyskobol

import com.typesafe.config.Config
import pl.dyskobol.prototype.DyskobolSystem

import scala.reflect.runtime.universe
import scala.tools.reflect.ToolBox

abstract class Wrapper {
  def run(conf: Config): Unit
}

private object Main extends App {
  def getSrcCode(conf:Config): String =
    s"""
      import pl.dyskobol._
      import akka.stream._
      import akka.stream.scaladsl.GraphDSL.Implicits._
      import com.typesafe.config.Config
      import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
      import pl.dyskobol.prototype.stages.GeneratedFilesBuffer
      import pl.dyskobol.prototype.{DyskobolSystem, plugins, stages}
      private class WrapperImpl extends Wrapper {

        override def run(conf: Config): Unit = {
            clearLogFile()
            DyskobolSystem.run{implicit builder => sink =>
              ${conf.getObject("dyskobol").toConfig.getString("flow")}
              ClosedShape
            } {
              println("COMPLETED")
            }
          }
        }
      scala.reflect.classTag[WrapperImpl].runtimeClass
    """.stripMargin


  val tb = universe.runtimeMirror(getClass.getClassLoader).mkToolBox()
  if (args.length < 1) {
    println("No configuration file provided")
  } else {
    val conf: Config = DyskobolSystem.readConfig(args(0))
    val classDef = tb.parse {
      getSrcCode(conf)
    }
    val clazz = tb.compile(classDef).apply().asInstanceOf[Class[Wrapper]]
    val instance = clazz.getConstructor().newInstance()
    instance.run(conf)

  }



}