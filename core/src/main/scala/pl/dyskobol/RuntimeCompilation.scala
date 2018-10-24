package pl.dyskobol

import pl.dyskobol.prototype.DyskobolSystem

import scala.reflect.runtime.universe
import scala.tools.reflect.ToolBox

private object RuntimeCompilationTest extends App {
  val tb = universe.runtimeMirror(getClass.getClassLoader).mkToolBox()
  if (args.length < 1) {
    println("No configuration file provided")
  } else {
    val conf = DyskobolSystem.readConfig(args(0))
    val classDef = tb.parse {
      s"""
         |
         |import akka.stream._
         |import akka.stream.scaladsl.GraphDSL.Implicits._
         |import com.typesafe.config.Config
         |import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
         |import pl.dyskobol.prototype.stages.GeneratedFilesBuffer
         |import pl.dyskobol.prototype.{DyskobolSystem, plugins, stages}
         |
         |private class Wrapper extends Function[Array[String] ,Unit]{
         |  override def apply(args: Array[String] ): Unit = {
         |
         |  val config = DyskobolSystem.readConfig(args(0))
         |  run(config)
         |
         |
         |  def run(conf: Config): Unit = {
         |    clearLogFile()
         |
         |    DyskobolSystem.run{implicit builder => sink =>
         |
         |      ${conf.getObject("dyskobol").toConfig.getString("flow")}
         |
         |      ClosedShape
         |    } {
         |      println("COMPLETED")
         |    }
         |  }
         |}
         |}
         |
         |
         |scala.reflect.classTag[Wrapper].runtimeClass
    """.stripMargin
    }


    val clazz = tb.compile(classDef).apply().asInstanceOf[Class[Function[Array[String],Unit]]]

    val instance = clazz.getConstructor().newInstance()
    println(instance.apply(args))

  }



}