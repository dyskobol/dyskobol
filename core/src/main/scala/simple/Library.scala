package simple

import ch.jodersky.jni.nativeLoader
import pl.dyskobol.model.File


@nativeLoader("demo0")
object Library {

  @native def say(message: String): Int

  @native def openImgNat(path: String): Long

  @native def openFsNat(image: Long): Long

  @native def getDirFilesNat(filesystem: Long, path: String): Array[File]
}
