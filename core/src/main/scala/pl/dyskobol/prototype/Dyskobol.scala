package pl.dyskobol.prototype

import simple.Library


object Dyskobol extends App {
  val result = Library.say("hello world")
  assert(result == 42)
  Library.openImgNat( "asdad",1)

}
