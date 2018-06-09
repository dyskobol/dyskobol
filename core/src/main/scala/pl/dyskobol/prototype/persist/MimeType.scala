package pl.dyskobol.prototype.persist

import pl.dyskobol.prototype.persist.Tables.{MimeType, MimeTypes}
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.concurrent._
import ExecutionContext.Implicits.global

object Tables {

  case class MimeType(id: Int, mime: String, name: String)

  class MimeTypes(tag: Tag)
    extends Table[(Int, String, String)](tag, "MIMETYPES") {
    def id = column[Int]("ID", O.PrimaryKey/*, O.AutoInc*/)

    def mimetype = column[String]("MIMETYPE")

    def name = column[String]("NAME")

    def * = (id, mimetype, name) //<> (MimeType.tupled, MimeType.unapply)
  }


}

 object Main extends App{
   val mimetypes = TableQuery[MimeTypes]

   val db = Database.forURL("jdbc:postgresql://localhost/postgres?user=postgres&password=postgres")

   try {
     Await.result(db.run(DBIO.seq(

       mimetypes += (1, "mimetype", "name"),
       mimetypes += (2, "mimetype", "name"),

       // print the users (select * from USERS)
       mimetypes.result.map(println))), Duration.Inf)
   } finally db.close
 }
