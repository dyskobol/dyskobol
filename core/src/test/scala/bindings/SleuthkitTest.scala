package bindings

import java.io.FileNotFoundException
import java.nio.file.{FileSystems, Files, Path}

import org.scalatest._

class SleuthkitTest extends FunSuite with Matchers {
  test("check if openImgNat throws a proper exception") {
    assertThrows[FileNotFoundException] {
      Sleuthkit.openImgNat("path/that/doesn't/exist")
    }
  }

  test("check if openFsNat throws a proper exception") {
    assertThrows[FileNotFoundException] {
      Sleuthkit.openFsNat(0)
    }
  }

  test("readNat") {
    val filesystem = Sleuthkit.openFsNat( Sleuthkit.openImgNat("./core/res/test.iso") )
    val file = Sleuthkit.openFileNat(filesystem, Sleuthkit.getFileInode(filesystem, "./IMAGES/JPG/INVALID.JPG"))
    val expected = Files.readAllBytes(FileSystems.getDefault.getPath("./core/res/invalid.jpg"))
    val read = Sleuthkit.readNat(file, 0, expected.size)

    read should be (expected)
  }

  test("readNatToBuf") {
    val filesystem = Sleuthkit.openFsNat( Sleuthkit.openImgNat("./core/res/test.iso") )
    val file = Sleuthkit.openFileNat(filesystem, Sleuthkit.getFileInode(filesystem, "./IMAGES/JPG/INVALID.JPG"))
    val expected = Files.readAllBytes(FileSystems.getDefault.getPath("./core/res/invalid.jpg"))
    val read = Array.ofDim[Byte](expected.size)
    Sleuthkit.readToBufferNat(file, 0, expected.size, read, 0)

    read should be (expected)
  }
}
