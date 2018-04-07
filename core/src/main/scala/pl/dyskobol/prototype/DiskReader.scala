package pl.dyskobol.prototype

import akka.actor.{Actor, ActorLogging, ActorRef}
import pl.dyskobol.model.File
import pl.dyskobol.prototype.Dyskobol.{counter, getMimeType, img, printFilesRec}
import simple.Library

class DiskReader(val path: String, val dispatcher: ActorRef)  extends Actor with ActorLogging {

  //TODO:
  //chcę zaincalizować to w preStart, ale krzyczy, ze nie można drugi raz coś do vala przypisać, więc dałem var
  var filesystem : Long = 0

  override def preStart() {
    log.info("opening a filesystem ")
    filesystem = Library.openFsNat(Library.openImgNat(path))
    log.info("opened")
  }
  override def postStop() {
    log.info("close filesystem")
  }


  

  override def receive ={

    case "start" => {
      log.info("starting ")
      digg(".")
    }

    case message => log.info(message.toString)

  }

  def digg(rootPath: String): Unit = {
    val files = Library.getDirFilesNat(filesystem, rootPath)
    for(file <- files if file.name != "." && file.name != "..") {
      if( file.`type` == File.REGULAR_FILE){
        dispatcher ! file
      }else{
        if( file.`type` == File.DIRECTORY ) {
          val filePath: String = f"${rootPath}/${file.name}"
          digg(filePath)
        }
      }


    }
  }

}
