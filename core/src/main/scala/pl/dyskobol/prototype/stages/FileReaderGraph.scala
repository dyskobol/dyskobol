package pl.dyskobol.prototype.stages

import java.util.TimerTask

import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{Attributes, Graph, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import pl.dyskobol.model.{File, FilePointer, FileProperties, FlowElements}
import pl.dyskobol.prototype.plugins.file
import bindings.Sleuthkit

import scala.concurrent.Future

class FileReaderGraph(val path: String, val timeout: Long = 1000)(implicit val bufferedGenerated: Option[GeneratedFilesBuffer] = None) extends GraphStage[SourceShape[FlowElements]] {
  val out: Outlet[FlowElements] = Outlet("Files")
  override val shape: SourceShape[FlowElements] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {

      private val filesystem : Long = Sleuthkit.openFsNat(Sleuthkit.openImgNat(path))
      private val root: FilePointer = Sleuthkit.getFileInode(filesystem, ".")

      private var files: List[File] = Nil
      private var generatedFiles: Iterator[File] = Iterator.empty

      // We need to keep track of path and file pointers
      private var directoriesStack: List[Tuple2[String, FilePointer]] = ("", root) :: Nil


      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          val file = nextFile()
          if( file.isDefined ) {
            push(out, (file.get, new FileProperties))
          }
          else {
            waitForNewFiles()
          }
        }
      })

      def nextFile(): Option[File] = {
        // Generated outside
        if( generatedFiles.hasNext ) {
          return Some(generatedFiles.next())
        } else
        if( bufferedGenerated.isDefined) {
          generatedFiles = bufferedGenerated.get.empty()
          if( generatedFiles.hasNext ) {
            return Some(generatedFiles.next())
          }
        }

        // Generated from sleuthkit
        if( files.nonEmpty ) {
          val toReturn = files.head
          files = files.tail
          return Some(toReturn)
        }

        if( directoriesStack.isEmpty)  None
        else {
          var newFiles = List[File]()

          // There may be empty directories
          while(newFiles.isEmpty && directoriesStack.nonEmpty) newFiles = readNewFiles()

          // No more directories
          if(newFiles.isEmpty) {
            return None
          }

          files = newFiles.tail ++ files
          Some(newFiles.head)
        }
      }

      def readNewFiles(): List[File] = {
        val (path, filePointer) = directoriesStack.head
        val files = Sleuthkit.getDirFilesByInodeNat(filesystem, filePointer).toList
        directoriesStack = directoriesStack.tail
        val normalFiles = files withFilter (f => f.name != ".." && f.name != ".") map (f => {f.path = path; f} )
        val directoriesToProcess = normalFiles.filter(_.`type` == File.DIRECTORY).map( f => (s"${path}/${f.name}", f.addr) )
        directoriesStack = directoriesToProcess ++ directoriesStack
        normalFiles
      }

      def waitForNewFiles(): Unit = {
        // No additional dynamic files
        if( bufferedGenerated.isEmpty ) {
          complete(out)
          return
        }

        // Additional generated files
        // If no file is generated in given time we can terminate the processing
        var shouldComplete = true
        new java.util.Timer().schedule(new TimerTask {
          override def run(): Unit = {
            if(shouldComplete) {
              complete(out)
            }
          }
        }, timeout)

        // Make sure the system is not terminated if a file comes
        bufferedGenerated.get.setFillCallback(() => {
          shouldComplete = false
          push(out, (nextFile().get, new FileProperties))
          bufferedGenerated.get.setFillCallback(None)
        })
      }
    }
}
