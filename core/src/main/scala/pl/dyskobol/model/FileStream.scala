package pl.dyskobol.model

import java.io.InputStream

import bindings.Sleuthkit

class FileStream(val filesystem: Long, val file: File) extends InputStream {
  var opened = false
  private var fileHandle: Long = 0
  private var fileOffset: Long = 0
  private var mark: Long = 0
  openFile()

  private def openFile(): Unit = {
    if( opened ) return
    fileHandle = Sleuthkit.openFileNat(filesystem, file.addr)
    opened = true
  }

  override def close(): Unit = {
    if( !opened ) return

    Sleuthkit.closeFileNat(fileHandle)
    fileHandle = 0
    opened = false
  }

  override def read(): Int = {
    val bytes = Sleuthkit.readNat(fileHandle, fileOffset, 1)
    if (bytes.length > 0) {
      fileOffset += 1
      bytes(0)
    }
    else -1
  }

  override def available(): Int = {
    return (file.size - fileOffset).toInt
  }

  override def mark(i: Int): Unit = {
    mark = i
  }

  override def markSupported(): Boolean = true

  override def read(bytes: Array[Byte], bufferOffset: Int, len: Int): Int = {
    val read = Sleuthkit.readToBufferNat(fileHandle, fileOffset, len, bytes, bufferOffset).toInt
    if( read > 0 ) fileOffset += read
    read
  }

  override def read(bytes: Array[Byte]): Int = read(bytes, 0, bytes.length)

  override def reset(): Unit = {
    fileOffset = mark
    mark = 0
  }

  override def skip(tryToSkip: Long): Long = {
    val canBeSkipped = file.size - fileOffset
    val skipping = if (tryToSkip < canBeSkipped) tryToSkip else canBeSkipped
    fileOffset += skipping
    return skipping
  }

  override def finalize(): Unit = close()
}
