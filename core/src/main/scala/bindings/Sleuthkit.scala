package bindings

import ch.jodersky.jni.nativeLoader
import pl.dyskobol.model.File


@nativeLoader("bindings_tsk")
object Sleuthkit {

  @native def openImgNat(path: String): Long

 @native def getImgSize(image: Long): Long

  @native def openFsNat(image: Long): Long

  @native def getDirFilesNat(filesystem: Long, path: String): Array[File]

  @native def getDirFilesByInodeNat(filesystem: Long, inode: Long): Array[File]

  @native def getFileInode(filesystem: Long, path: String): Long

  @native def openFileNat(filesystem: Long, inode: Long): Long

  @native def closeFileNat(file: Long)

  @native def readNat(file: Long, offset: Long, count: Long): Array[Byte]

  @native def readToBufferNat(file: Long, fileOffset: Long, count: Long, buffer: Array[Byte], bufferOffset: Long): Long
}
