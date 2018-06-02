package pl.dyskobol.prototype.stages

import akka.stream.{Attributes, Graph, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import pl.dyskobol.model.{File, FilePointer, FileProperties, FlowElements}
import simple.Library

class FileReaderGraph(val path: String) extends GraphStage[SourceShape[FlowElements]] {
  val out: Outlet[FlowElements] = Outlet("Files")
  override val shape: SourceShape[FlowElements] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {

      private val filesystem : Long = Library.openFsNat(Library.openImgNat(path))
      private val root: FilePointer = Library.getFileInode(filesystem, ".")

      private var files: List[File] = Nil
      private var directoriesStack: List[FilePointer] = root :: Nil

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          val file = getNext()
          if( file.isDefined ) push(out, (file.get, new FileProperties)) else {
            complete(out)
          }
        }
      })

      def getNext(): Option[File] = {
        if( files.nonEmpty ) {
          val toReturn = files.head
          files = files.tail
          return Some(toReturn)
        }

        if( directoriesStack.isEmpty)  None
        else {
          val newFiles = readNewFiles()
          files = newFiles.tail ++ files
          Some(newFiles.head)
        }
      }

      def readNewFiles(): List[File] = {
        val files = Library.getDirFilesByInodeNat(filesystem, directoriesStack.head).toList
        directoriesStack = directoriesStack.tail
        val normalFiles = files filter (f => f.name != ".." && f.name != ".")
        val directoriesPointers = normalFiles.filter(_.`type` == File.DIRECTORY).map(_.addr)
        directoriesStack = directoriesPointers ++ directoriesStack
        normalFiles
      }
    }
}
