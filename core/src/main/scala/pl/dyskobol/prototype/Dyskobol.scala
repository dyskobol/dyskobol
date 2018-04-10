package pl.dyskobol.prototype

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.util.stream.Collectors
import java.io.InputStream
import java.nio.charset.Charset

import pl.dyskobol.model.{File, FileStream}
import org.apache.tika.Tika
import pl.dyskobol.prototype.Dyskobol.filesystem
import simple.Library

import scala.collection.mutable

object Dyskobol extends App {
  var counter = 0;

  val img = Library.openImgNat("/home/kuba/downloads.iso")
  println("Image opened")

  val filesystem = Library.openFsNat(img)
  println("Filesystem opened")

  printFilesRec(filesystem, Library.getFileInode(filesystem, "."))
  println(f"Files processed: ${counter}")

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

  def printFilesRec(filesystem: Long, inode: Long): Unit = {
    val files = Library.getDirFilesByInodeNat(filesystem, inode)
    for(file <- files if file.name != "." && file.name != "..") {
      counter += 1
      if( counter % 1000 == 0 ) {
        println(counter)
      }

      if( file.`type` == File.REGULAR_FILE ) { // File
//        println(f"\n${path}${file.name} ${file.`type`}")
        val fileStream = file.createStream()
//        println(f"Guessed file content type is ${getMimeType(file, fileStream)}")
        fileStream.close()
      }
      else
      if( file.`type` == File.DIRECTORY ) {
//        println("PATH = "+ filePath)
        printFilesRec(filesystem, file.addr)
      }
    }
  }


}