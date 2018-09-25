package pl.dyskobol.prototype.persistance

import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.persistance.Tables._
import slick.driver.PostgresDriver
import slick.jdbc.meta.{MQName, MTable}
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import pl.dyskobol.prototype.Main.DUMMY_DB

class DB(val hostName: String, val dbName: String, val username: String, val password: String) {
  val files = TableQuery[FileInfo]
  def filesWithId =
    (files returning files.map(_.id)).into((_, id) => id)
  val stringValues = TableQuery[StringValues]
  val byteValues = TableQuery[ByteValues]
  val numberValues = TableQuery[NumberValues]
  val dateValues = TableQuery[DateValues]

  val tables = List( stringValues.schema, numberValues.schema, dateValues.schema, byteValues.schema, files.schema)
  val schema = stringValues.schema ++ numberValues.schema ++ dateValues.schema ++ byteValues.schema ++ files.schema

    implicit val db = Database.forURL(f"jdbc:postgresql://${hostName}/${dbName}?user=${username}&password=${password}")


    val tableNames = List("STRING_PROPERTIES", "NUMBER_PROPERTIES", "DATE_PROPERTIES", "BYTE_PROPERTIES", "FILES")

    val tablesInDatabase = Await.result(db.run(MTable.getTables), Duration.Inf).toList

    for (i <- tableNames) {
      var table = false
      for (MTable(MQName(_, _, j), _, _, _, _, _) <- tablesInDatabase) {
        if (i == j) {
          table = true
        }
      }
      if (!table)
        Await.result(db.run(tables(tableNames.indexOf(i)).create), Duration.Inf)
    }


    def saveFiles(files: Iterable[File]) = {
      val rows = files.map(file => (0, file.name, file.mime)).toList

        val run = db.run {
          filesWithId ++= rows
        }
        run.map(r => {
          r.zip(files).foreach(idAndFile => {
            val (fileId, file) = idAndFile
            file.id = fileId
            file
          })
        })
    }

    def saveProps(properties: Iterable[Tuple2[Int, FileProperties]]): Future[Unit] = {

      val rows = properties.flatMap(tuple => {
        val (fileId, props) = tuple
        props.getAll().map((nameAndValue) => {
          val (name, value) = nameAndValue
          value match {
            case v: String => row(fileId, name, v)
            case v: Array[Byte] => row(fileId, name, v)
            case v: java.util.Date => row(fileId, name, v)
            case v: BigDecimal => row(fileId, name, v)
          }
        })
      })

      db.run(DBIO.seq(rows.toSeq: _*))
    }

    def close() = db.close()

    private def fileRow(name: String, mimetype: String)

    = filesWithId += (0, name, mimetype)

    private def row(fileId: Int, name: String, value: String)

    = stringValues += (fileId, name, value)

    private def row(fileId: Int, name: String, value: Array[Byte])

    = byteValues += (fileId, name, value.asInstanceOf)

    private def row(fileId: Int, name: String, value: java.util.Date)

    = dateValues += (fileId, name, value.asInstanceOf)

    private def row(fileId: Int, name: String, value: BigDecimal)

    = numberValues += (fileId, name, value)

}