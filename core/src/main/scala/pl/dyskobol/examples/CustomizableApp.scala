package pl.dyskobol.examples

import akka.actor.ActorRef
import akka.{Done, NotUsed}
import akka.stream._
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.GraphDSL.Implicits._
import com.google.inject.Guice
import com.typesafe.config.Config
import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.customstages.GeneratedFilesBuffer
import pl.dyskobol.prototype.plugins.document.{DocumentContentExtract, DocumentMetadataExtract}
import pl.dyskobol.prototype.plugins.dummyDb.flows.clearLogFile
import pl.dyskobol.prototype.plugins.file.FileMetadataExtract
import pl.dyskobol.prototype.plugins.image.ImageMetaExtract
import pl.dyskobol.prototype.plugins.metrics.ProcessMonitor
import pl.dyskobol.prototype.plugins.plugin
import pl.dyskobol.prototype.{DyskobolModule, DyskobolSystem, plugins, stages}

import scala.concurrent.Future


object CustomizableApp extends Process {
  override val configOptions: Map[String, String] = Map(
    "unzip" -> "unzip files",
    "img" -> "extract image metadata",
    "docs" -> "extract documents content",
    "docsm" -> "extract documents content",
    "meta" -> "extract basic file metadata",
    "stats" -> "display stats after processing",
  )


  def attachPlugin(implicit  builder: GraphDSL.Builder[Future[Done]],
                   broadcast: UniformFanOutShape[(File, FileProperties), (File, FileProperties)],
                   merge: UniformFanInShape[(File, FileProperties), (File, FileProperties)],
                   plugin: plugin,
                   processMonitor: ActorRef,
                   stats: Boolean) = {
    val p = builder add plugin.flow()
    if(stats){
      val (checkIn, checkOut) = plugins.metrics.ProcessingTimeGateways(plugin.name)
      broadcast ~> checkIn ~> p ~> checkOut ~> merge
    }else{
      broadcast ~> p ~> merge
    }
  }

  def run(conf: Config): Unit = {
    clearLogFile()
    val processConfig = conf.getObject("dyskobol.process").toConfig
    val injector = Guice.createInjector(new DyskobolModule())
    val dyskobolSystem = injector.getInstance(classOf[DyskobolSystem])
    val all = processConfig.getBoolean("all")

    val img = processConfig.getBoolean("img") || all
    val docs = processConfig.getBoolean("docs") || all
    val docsm = processConfig.getBoolean("docsm") || all
    val meta = processConfig.getBoolean("meta") || all
    val unzip = processConfig.getBoolean("unzip") || all
    val mergeNeeded = img || docs || docsm || meta
    val stats = processConfig.getBoolean("stats") || all
    val mergeSize = List(img, docs, docsm, meta).count(p => p)
    val broadcastSize = List(img, docs, docsm, meta, unzip, !mergeNeeded).count(p => p)

    dyskobolSystem.run{ implicit processMonitor => implicit builder => sink =>

    implicit val bufferedGenerated = new GeneratedFilesBuffer
      var merge: UniformFanInShape[(File, FileProperties), (File, FileProperties)] =  null
      var persistProps: FlowShape[(File, FileProperties), (File, FileProperties)] = null
      val source          = builder add  stages.VfsFileSource(processConfig.getString("imagePath"))
      val broadcast       = builder add stages.Broadcast(broadcastSize)
      val mimeResolver    = builder add plugins.filetype.flows.resolver
      val persistFiles = builder add plugins.dummyDb.flows.PersistFiles()

      mimeResolver ~> persistFiles ~> broadcast

      if(mergeNeeded){
        merge = builder add stages.Merge(mergeSize)
        persistProps =  builder add plugins.dummyDb.flows.PersistProps()
        merge ~> persistProps
        if(stats){
          val (checkIn, checkOut) = plugins.metrics.ProcessingTimeGateways("process")
          source ~> checkIn ~> mimeResolver
          persistProps ~> checkOut ~> sink
        }else{
          source ~> mimeResolver
          persistProps ~> sink
        }
      }else{
        source ~> mimeResolver
        broadcast ~> sink
      }


      if(docs)
        attachPlugin(builder, broadcast, merge, new DocumentContentExtract(), processMonitor, stats)

      if(docsm)
        attachPlugin(builder, broadcast, merge, new DocumentMetadataExtract(), processMonitor, stats)

      if(img)
        attachPlugin(builder, broadcast, merge, new ImageMetaExtract("image/jpeg"::"image/tiff"::Nil), processMonitor, stats)

      if(meta)
        attachPlugin(builder, broadcast, merge, new FileMetadataExtract(), processMonitor, stats)

      if(unzip){
       broadcast ~> (builder add plugins.unzip.filesGenerators.unzip)
      }

      ClosedShape
    } {
      println("COMPLETED")
    }
  }

}