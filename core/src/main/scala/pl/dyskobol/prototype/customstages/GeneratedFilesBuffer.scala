package pl.dyskobol.prototype.customstages

import pl.dyskobol.model.File

import scala.collection.immutable.Stream.Empty

class GeneratedFilesBuffer {
  private var generatedFiles: Iterator[File] = Iterator.empty

  def fill(files: Iterator[File]): Unit = if( files.hasNext ) {
    generatedFiles.synchronized {
      generatedFiles = generatedFiles ++ files
    }
  }

  def empty(): Iterator[File] = generatedFiles.synchronized {
      val toReturn = generatedFiles
      generatedFiles = Iterator.empty
      return toReturn
  }
}
