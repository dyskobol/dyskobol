package pl.dyskobol.prototype

import simple.Library


object Dyskobol extends App {
  val result = Library.say("hello world")
  assert(result == 42)
//  Library.openImgNat( "asdad")
  val img = Library.openImgNat("/home/kuba/xd.iso")
  val filesystem = Library.openFsNat(img)
  val files = Library.getDirFilesNat(filesystem, ".")
  for(file <- files) {
    println(file.name)
  }
}
