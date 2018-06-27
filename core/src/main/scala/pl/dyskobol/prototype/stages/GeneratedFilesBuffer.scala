package pl.dyskobol.prototype.stages

import pl.dyskobol.model.File

import scala.collection.immutable.Stream.Empty

class GeneratedFilesBuffer {
  private var generatedFiles: Iterator[File] = Iterator.empty
  private var callOnFill: Option[()=>Unit] = None

  def fill(files: Iterator[File]): Unit = if( files.hasNext ) {
    generatedFiles.synchronized {
      generatedFiles = generatedFiles ++ files
    }
    if( callOnFill.isDefined ) {
      callOnFill.get()
    }
  }

  def empty(): Iterator[File] = generatedFiles.synchronized {
      val toReturn = generatedFiles
      generatedFiles = Iterator.empty
      return toReturn
  }

  def setFillCallback(callOnFill: ()=>Unit): Unit = {
    this.callOnFill = Option(callOnFill)
  }

  def setFillCallback(callOnFill: Option[()=>Unit] = None): Unit = {
    this.callOnFill = callOnFill
  }

}
