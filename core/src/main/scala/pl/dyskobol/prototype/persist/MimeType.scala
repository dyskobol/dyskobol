package pl.dyskobol.prototype.persist

import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

object Tables {

  case class MimeType(id: Int, mime: String, name: String)

  class MimeTypes(tag: Tag)
    extends Table[(Int, String, String)](tag, "MIMETYPES") {
    def id = column[Int]("ID", O.PrimaryKey/*, O.AutoInc*/)

    def mimetype = column[String]("MIMETYPE")

    def name = column[String]("NAME")

    def * = (id, mimetype, name) //<> (MimeType.tupled, MimeType.unapply)
  }

  class Properties(tag: Tag)
    extends Table[(Int, String, String)](tag, "PROPERTIES") {
    def id = column[Int]("ID")
    def property = column[String]("PROPERTY")
    def propertyValue = column[String]("PROPERTY_VALUE")
    def * = (id, property, propertyValue)
  }

}