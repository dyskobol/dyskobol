package pl.dyskobol.prototype.stages

import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{Attributes, Graph, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}
import pl.dyskobol.model.{File, FilePointer, FileProperties, FlowElements}
import simple.Library

class FileReaderGraph(val path: String)(generator: FilesGenerator = (_) => Iterator.empty) extends GraphStage[SourceShape[FlowElements]] {
  val out: Outlet[FlowElements] = Outlet("Files")
  override val shape: SourceShape[FlowElements] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {

      private val filesystem : Long = Library.openFsNat(Library.openImgNat(path))
      private val root: FilePointer = Library.getFileInode(filesystem, ".")

      private var files: List[File] = Nil
      private var generatedFiles: Iterator[File] = Iterator.empty

      // We need to keep track of path and file pointers
      private var directoriesStack: List[Tuple2[String, FilePointer]] = ("", root) :: Nil


      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          val file = getNext()
          if( file.isDefined ) {

            // Add new files (for example unwrapped files)
            try {
              generatedFiles = generatedFiles ++ generator(file.get)
            } catch {
              // Ignore
              case _: Throwable => ()
            }

            push(out, (file.get, new FileProperties))
          }
          else {
            complete(out)
          }
        }
      })

      def getNext(): Option[File] = {
        if( generatedFiles.hasNext ) {
          return Some(generatedFiles.next())
        }
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
        val files = Library.getDirFilesByInodeNat(filesystem, filePointer).toList
        directoriesStack = directoriesStack.tail
        val normalFiles = files withFilter (f => f.name != ".." && f.name != ".") map (f => {f.path = path; f} )
        val directoriesToProcess = normalFiles.filter(_.`type` == File.DIRECTORY).map( f => (s"${path}/${f.name}", f.addr) )
        directoriesStack = directoriesToProcess ++ directoriesStack
        normalFiles
      }
    }
}
