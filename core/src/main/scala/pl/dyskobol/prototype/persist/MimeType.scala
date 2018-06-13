package pl.dyskobol.prototype.persist

import slick.driver.PostgresDriver.api._
import slick.lifted.Tag
import java.sql.{Blob, Date}

object Tables {

  case class MimeType(id: Int, mime: String, name: String)

  class MimeTypes(tag: Tag)
    extends Table[(Int, String, String)](tag, "MIMETYPES") {
    def id = column[Int]("ID", O.PrimaryKey/*, O.AutoInc*/)

    def mimetype = column[String]("MIMETYPE")

    def name = column[String]("NAME")

    def * = (id, mimetype, name)
  }

  class StringValues(tag: Tag)
    extends Table[(Int,String, String)](tag, "STRING_PROPERTIES") {
    def id = column[Int]("ID")
    def name = column[String]("PROPERTY_NAME")
    def propertyValue = column[String]("PROPERTY_VALUE")
    def * = (id, name, propertyValue)
  }

  class NumberValues(tag: Tag)
    extends Table[(Int, String, BigDecimal)](tag, "NUMBER_PROPERTIES") {
    def id = column[Int]("ID")
    def name = column[String]("PROPERTY_NAME")
    def propertyValue = column[BigDecimal]("VALUE")
    def * = (id, name, propertyValue)
  }

  class DateValues(tag: Tag)
    extends Table[(Int, String, Date)](tag, "DATE_PROPERTIES") {
    def id = column[Int]("ID")
    def name = column[String]("PROPERTY_NAME")
    def propertyValue = column[Date]("VALUE")
    def * = (id, name, propertyValue)
  }

  class ByteValues(tag: Tag)
    extends Table[(Int, String, Blob)](tag, "BYTE_PROPERTIES") {
    def id = column[Int]("ID")
    def name = column[String]("PROPERTY_NAME")
    def propertyValue = column[Blob]("VALUE")
    def * = (id, name, propertyValue)
  }

}