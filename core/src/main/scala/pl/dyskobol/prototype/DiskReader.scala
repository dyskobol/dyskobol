package pl.dyskobol.prototype

import akka.actor.{Actor, ActorLogging, ActorRef}
import pl.dyskobol.model.File
import pl.dyskobol.prototype.Dyskobol.{counter, getMimeType, img, printFilesRec}
import simple.Library

import scala.annotation.tailrec

class DiskReader(val path: String, val dispatcher: ActorRef) {

  var filesystem : Long = Library.openFsNat(Library.openImgNat(path))

  final def digg(rootPath: String = "."): Unit = {
    val files = Library.getDirFilesNat(filesystem, rootPath)
    for(file <- files if file.name != "." && file.name != "..") {
      if( file.`type` == File.REGULAR_FILE){
        dispatcher ! file
      } else if( file.`type` == File.DIRECTORY ) {
          val filePath: String = f"${rootPath}/${file.name}"
          digg(filePath)
      }
    }
  }

}
