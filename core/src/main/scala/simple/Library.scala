package simple

import ch.jodersky.jni.nativeLoader
import pl.dyskobol.model.File


@nativeLoader("demo0")
object Library {

  @native def say(message: String): Int

  @native def openImgNat(path: String): Long

  @native def openFsNat(image: Long): Long

  @native def getDirFilesNat(filesystem: Long, path: String): Array[File]

  @native def openFileNat(filesystem: Long, inode: Long): Long

  @native def closeFileNat(file: Long)

  @native def readNat(file: Long, offset: Long, count: Long): Array[Byte]

  @native def readToBufferNat(file: Long, fileOffset: Long, count: Long, buffer: Array[Byte], bufferOffset: Long): Long
}
