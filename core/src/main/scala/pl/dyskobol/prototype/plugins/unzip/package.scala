package pl.dyskobol.prototype.plugins

import java.util.zip.ZipInputStream

import pl.dyskobol.model.{File, FileStream, FlowElements}
import pl.dyskobol.prototype.plugins.document.{DocumentContentExtract, DocumentMetadataExtract}
import pl.dyskobol.prototype.stages.FilesGenerator
import pl.dyskobol.prototype.customstages.GeneratedFilesBuffer
import org.apache.commons.io.IOUtils
import java.io.{FileInputStream, FileOutputStream, InputStream}

import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Flow



package object unzip {
  object filesGenerators {

    def unzip(implicit buffer: GeneratedFilesBuffer) = Sink.foreach[FlowElements](fe => {
      val (file, _) = fe
      buffer.fill( unzipFunction(file) )
    })

    private val unzipFunction: FilesGenerator = (file: File) => if( file.mime != "application/zip") Iterator.empty else {
      val zis = new ZipInputStream(file.createStream())
      new Iterator[File] {
        var nextEntry = zis.getNextEntry()
        var buffer = Array.fill[Byte](4096)(0)
        val path = f"${file.path}/${file.name}@"

        override def hasNext: Boolean = nextEntry != null

        override def next(): File = {
          val fileType = if (nextEntry.isDirectory) File.DIRECTORY else File.REGULAR_FILE

          // We need to create a tmp file, because there might be multiple input streams opened
          val tmpFile = readToTmpFile()

          val entry = nextEntry
          try {
            nextEntry = zis.getNextEntry
          } catch {
            case e: Throwable => e.printStackTrace()
          }
          new File(entry.getName, path, fileType, 0, 0, entry.getSize, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0) {

            override def createStream(): InputStream = {
              val self = this
              new FileInputStream(tmpFile) {
                // We need to keep reference to parent file to make sure the file dies after the stream - we need to delete tmp file
                val openedBy: File = self
              }
            }

            override def finalize(): Unit = {
              super.finalize()
              tmpFile.delete()
            }

          }
        }


        def readToTmpFile(): java.io.File = {
          import java.io.File
          val tmpFile = File.createTempFile("unzipped-", "")
          tmpFile.deleteOnExit()
          val output = new FileOutputStream(tmpFile)
          try {
            var len = zis.read(buffer)
            while ( len > 0 ) {
              output.write(buffer, 0, len)
              len = zis.read(buffer)
            }
            tmpFile
          } finally {
            output.close()
          }
        }
      }
    }
  }
}
