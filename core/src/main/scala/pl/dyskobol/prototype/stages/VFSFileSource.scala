package pl.dyskobol.prototype.stages

import java.util.{TimerTask, concurrent}
import java.util.concurrent.{TimeUnit, TimeoutException}

import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{Attributes, Graph, Outlet, SourceShape}
import akka.stream.stage._
import pl.dyskobol.model.{File, FilePointer, FileProperties, FlowElements}
import pl.dyskobol.prototype.plugins.file
import bindings.Sleuthkit

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

class VFSFileSource(val path: String, val timeout: FiniteDuration = Duration(1, TimeUnit.SECONDS))(implicit val generatedFilesBuffer: Option[GeneratedFilesBuffer] = None) extends GraphStage[SourceShape[FlowElements]] {
  val out: Outlet[FlowElements] = Outlet("Files")
  override val shape: SourceShape[FlowElements] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new TimerGraphStageLogic(shape) with OutHandler {

      private val filesystem: Long = Sleuthkit.openFsNat(Sleuthkit.openImgNat(path))
      private val root: FilePointer = Sleuthkit.getFileInode(filesystem, ".")

      private var files: List[File] = Nil
      private var generatedFiles: Iterator[File] = Iterator.empty

      // We need to keep track of path and file pointers
      private var directoriesStack: List[(String, FilePointer)] = ("", root) :: Nil

      private var shouldFinish = false
      private var deadline: Long = 0

      setHandler(out, this)

      override def onPull(): Unit = {
        val file = nextFile()
        if (file.isDefined) {
          refreshDeadline()
          push(out, (file.get, new FileProperties))
        }
        else {
          waitForNewFiles()
        }
      }

      def nextFile(): Option[File] = {
        // Generated outside
        if (generatedFiles.hasNext) {
          return Some(generatedFiles.next())
        } else if (generatedFilesBuffer.isDefined ) {
          generatedFiles = generatedFilesBuffer.get.empty()
          if( generatedFiles.hasNext) return nextFile()
        }

        // Generated from sleuthkit
        if (files.nonEmpty) {
          val toReturn = files.head
          files = files.tail
          return Some(toReturn)
        }

        if (directoriesStack.isEmpty) None
        else {
          var newFiles = List[File]()

          // There may be empty directories
          while (newFiles.isEmpty && directoriesStack.nonEmpty) newFiles = readNewFiles()

          // No more directories
          if (newFiles.isEmpty) {
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
        val normalFiles = files withFilter (f => f.name != ".." && f.name != ".") map (f => {
          f.path = path; f
        })
        val directoriesToProcess = normalFiles.filter(_.`type` == File.DIRECTORY).map(f => (s"${path}/${f.name}", f.addr))
        directoriesStack = directoriesToProcess ++ directoriesStack
        normalFiles
      }

      // We can't use system timer callback, because all actions need to performed in the same thread
      final override protected def onTimer(key: Any): Unit = {
        if (!shouldFinish) {
          return
        }

        generatedFiles = generatedFilesBuffer.get.empty()
        if( generatedFiles.hasNext ) {
          shouldFinish = false
          onPull()
        }

        if (deadline - System.nanoTime < 0) {
          complete(out)
        }
      }

      def refreshDeadline(): Unit = {
        deadline = System.nanoTime() + timeout.toNanos
      }

      def waitForNewFiles(): Unit = {
        // No additional dynamic files
        if (generatedFilesBuffer.isEmpty) {
          complete(out)
          return
        }

        shouldFinish = true
        refreshDeadline()
      }

      // This is taken from Akka Stream timers
      override def preStart(): Unit = schedulePeriodically(this, {
        import scala.concurrent.duration._
        FiniteDuration(
          math.min(math.max(timeout.toNanos / 8, 100.millis.toNanos), timeout.toNanos / 2),
          concurrent.TimeUnit.NANOSECONDS
        )
      })
    }
}
