package pl.dyskobol.prototype
import org.sleuthkit.datamodel._;
import pl.dyskobol.prototype.io.DiscReader


object Dyskobol extends App {
  print("Hello world!")
  val dr = new DiscReader("/home/przemek/Dokumenty/agh/out.img")
  dr.list_disk()

}
