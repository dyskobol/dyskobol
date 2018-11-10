package plugins

import akka.{Done, NotUsed}
import akka.stream.{ClosedShape, FlowShape, Graph}
import com.google.inject.Guice
import org.scalatest.FunSuite
import pl.dyskobol.model.{File, FileProperties}
import akka.stream.scaladsl.GraphDSL.Implicits._


import scala.concurrent.duration._
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}

import scala.collection.mutable
import scala.concurrent.{Await, Future}

class AssertPlugin(val expectedProcessedFiles: Int, val expectedPropsExtractedFromFile: scala.collection.mutable.Map[String, Seq[(String, Any)]]) extends FunSuite with  plugins.plugin  {
  override def name: String = "assert plugin"


  val processed: mutable.Map[String, FileProperties] = mutable.Map[String, FileProperties]()

  override def flow(): Graph[FlowShape[(File, FileProperties), (File, FileProperties)], NotUsed] = {
    stages.ForEach(fe => {
      val filePath = s"${fe._1.path}/${fe._1.name}"
      processed += (filePath -> fe._2)
    })

  }

  def onCompleted(): Unit = {
    expectedPropsExtractedFromFile.foreach(kv=>{
      val (filePath, expectedProps) = kv
      val processedProp = this.processed(filePath)
      assert(processedProp != null, s"File ${filePath} wasn't processed")
      expectedProps.foreach(prop => {
        val (k, v) = prop
        assert(v.equals(processedProp.get(k)))

      })

    })

    Option(expectedProcessedFiles).foreach(size =>
      assert(this.processed.size.equals(size), s"Processed files: ${this.processed.size}, expected: ${this.expectedProcessedFiles}")
    )

  }
}


class PluginTest extends FunSuite {

  def testFlow(imagePath: String, plugin: Graph[FlowShape[(File, FileProperties), (File, FileProperties)], NotUsed],
               expectedProcessedFiles: Int, expectedPropsExtractedFromFile: scala.collection.mutable.Map[String, Seq[(String, Any)]]): Any = {



    val injector = Guice.createInjector(new DyskobolModule())
    val dyskobolSystem = injector.getInstance(classOf[DyskobolSystem])

    val assertionPlugin = new AssertPlugin(expectedProcessedFiles, expectedPropsExtractedFromFile)
    val result =  dyskobolSystem.run { implicit processMonitor =>implicit builder =>
          sink =>

            val source = builder add stages.VfsFileSource(imagePath)
            val testedPlugin = builder add plugin
            val verifier = builder add assertionPlugin.flow()
            val mimeResolver = builder add plugins.filetype.flows.resolver

            source ~> mimeResolver ~> testedPlugin~> verifier ~> sink

            ClosedShape
      } {}
    Await.result(result, 1.minute)
    assertionPlugin.onCompleted()

  }
}
