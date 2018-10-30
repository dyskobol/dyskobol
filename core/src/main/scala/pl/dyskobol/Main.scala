package pl.dyskobol

import com.typesafe.config.{Config, ConfigFactory}
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
      import akka.stream.scaladsl.{Balance, GraphDSL, Merge}
      import com.google.inject.Guice
      import com.typesafe.config.{Config, ConfigFactory}
      import pl.dyskobol.model.FlowElements
      import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
      import pl.dyskobol.prototype.plugins.metrics.Configure
      import pl.dyskobol.prototype.customstages.GeneratedFilesBuffer
      import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}

      private class WrapperImpl extends Wrapper {

        override def run(conf: Config): Unit = {
            clearLogFile()
            val injector = Guice.createInjector(new DyskobolModule())
            val dyskobolSystem = injector.getInstance(classOf[DyskobolSystem])
            dyskobolSystem.run{implicit processMonitor => implicit builder => sink =>
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
    val conf: Config = readConfig(args(0))
    val classDef = tb.parse {
      getSrcCode(conf)
    }
    val clazz = tb.compile(classDef).apply().asInstanceOf[Class[Wrapper]]
    val instance = clazz.getConstructor().newInstance()
    instance.run(conf)

  }
  def readConfig(path: String): Config = ConfigFactory.parseFile(new java.io.File(path))


}