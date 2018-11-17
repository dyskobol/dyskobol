package pl.dyskobol

import java.io.{BufferedReader, File, PrintWriter, StringReader}

import com.typesafe.config.{Config, ConfigFactory, ConfigValue, ConfigValueFactory}

import scala.tools.nsc.interpreter._
import javax.script._
import pl.dyskobol.prototype.DyskobolSystem
import pl.dyskobol._
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, GraphDSL, Merge}
import com.google.inject.Guice
import pl.dyskobol.model.FlowElements
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.plugins.metrics.Configure
import pl.dyskobol.prototype.customstages.GeneratedFilesBuffer
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}

import scala.collection.mutable
import scala.io.StdIn
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

  def readImgPath():String = {
    val filePath = StdIn.readLine("Please provide path to VFS file: ").trim
    if(new File(filePath).exists())
      return filePath
    else{
      println("That file doesn't exist")
      return readImgPath()
    }
  }

  def getProcess(): examples.Process = {
    val processKey = StdIn.readLine("Please choose process number to execute:").trim
    if (examples.processes.contains(processKey)){
      return examples.processes(processKey)._2
    }else{
      println("Enter valid key:")
      return getProcess()
    }
  }

  def buildConfiguration(process: examples.Process, conf: Config):Config = {
    println(process.helpMessage)
    var newConfig = conf
    val unrecognized = mutable.MutableList[String]()
    StdIn.readLine("Enter options:").trim.split("--").filter(!_.isEmpty).foreach(s => {
      val option = s.split(" +").mkString
      if (option.equals("all"))
      return newConfig.withValue(s"dyskobol.process.$option", ConfigValueFactory.fromAnyRef(true))

      if (process.configOptions.contains(option))
        newConfig = newConfig.withValue(s"dyskobol.process.$option", ConfigValueFactory.fromAnyRef(true))
      else
        unrecognized += option

    })
    if(unrecognized.isEmpty)
      return newConfig
    println(s"Unrecongized options: ${unrecognized.mkString(", ")}")
    return buildConfiguration(process, newConfig)
  }

  def runInteractiveApp() = {
    val defaultConfig =readConfig("./core/res/dyskobol.conf")
    val filePath = readImgPath()
    var conf = defaultConfig.withValue("dyskobol.process.imagePath", ConfigValueFactory.fromAnyRef(filePath))
    examples.introduce()
    val process = getProcess()
    if (process.configOptions.nonEmpty){
     conf = buildConfiguration(process, conf)
    }
    process.run(conf)

  }

  if (args.length < 1) {
    runInteractiveApp()
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