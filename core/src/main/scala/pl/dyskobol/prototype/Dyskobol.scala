package pl.dyskobol.prototype

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util.stream.Collectors
import java.io.InputStream
import java.nio.charset.Charset

import pl.dyskobol.model.{File, FileStream}
import org.apache.tika.Tika
import pl.dyskobol.prototype.Dyskobol.filesystem
import simple.Library

object Dyskobol extends App {
  val img = Library.openImgNat("/home/kuba/test/pliczki.iso")
  val filesystem = Library.openFsNat(img)
//  val files = Library.getDirFilesNat(filesystem, ".")
  printFiles(filesystem, ".")

  def readString(file: File, fileStream: FileStream, count: Int = 100): String = {
    val count_ = if (file.size < count) file.size.toInt else count
    val bytes: Array[Byte] = Array.ofDim[Byte](count_)
    val read = fileStream.read(bytes)
    println(f"Read ${read}")
    fileStream.mark(0)
    fileStream.reset()
    return new String(bytes)
  }

  def getMimeType(file: File, fileStream: FileStream): String = {
    val tika = new Tika
    tika.detect(fileStream, file.name)
  }

  def printFiles(filesystem: Long, path: String): Unit = {
    val files = Library.getDirFilesNat(filesystem, path)
    for(file <- files) {
      println(f"\n${file.name} ${file.`type`}")

      if( file.`type` == File.REGULAR_FILE ) { // File
        val fileStream = file.createStream()
        println(f"Guessed file content type is ${getMimeType(file, fileStream)}")
      }
//      else
//      if( file.`type` == File.DIRECTORY && file.name == "." && file.name == ".." ) {
//        printFiles(filesystem, f"${file.path}/${file.name}")
//      }
    }
  }
}
