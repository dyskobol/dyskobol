package bindings

import java.io.FileNotFoundException

import org.scalatest.FunSuite

class SleuthkitTest extends FunSuite{
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
}
