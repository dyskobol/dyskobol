package pl.dyskobol.prototype.persistance

import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.persistance.Tables._
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class DB(val dbName: String, val username: String, val password: String) {
  val files = TableQuery[FileInfo]
  def filesWithId =
    (files returning files.map(_.id)).into((_, id) => id)
  val stringValues = TableQuery[StringValues]
  val byteValues = TableQuery[ByteValues]
  val numberValues = TableQuery[NumberValues]
  val dateValues = TableQuery[DateValues]

  val tables = List( stringValues.schema, numberValues.schema, dateValues.schema, byteValues.schema, files.schema)
  val schema = stringValues.schema ++ numberValues.schema ++ dateValues.schema ++ byteValues.schema ++ files.schema

  val db = Database.forURL(f"jdbc:postgresql://localhost/${dbName}?user=${username}&password=${password}")

  //val tableNames = List("FILES", "STRING_PROPERTIES", "NUMBER_PROPERTIES", "DATE_PROPERTIES", "BYTE_PROPERTIES")

  val tablesInDatabase = Await.result(db.run(MTable.getTables), Duration.Inf).toList

//  if(tablesInDatabase.find(x => x == "FILES").isEmpty) Await.result(db.run(files.schema.create), Duration.Inf)
//  if(tablesInDatabase.find(x => x == "STRING_PROPERTIES").isEmpty) Await.result(db.run(stringValues.schema.create), Duration.Inf)
//  if(tablesInDatabase.find(x => x == "NUMBER_PROPERTIES").isEmpty) Await.result(db.run(numberValues.schema.create), Duration.Inf)
//  if(tablesInDatabase.find(x => x == "DATE_PROPERTIES").isEmpty) Await.result(db.run(dateValues.schema.create), Duration.Inf)
//  if(tablesInDatabase.find(x => x == "BYTE_PROPERTIES").isEmpty) Await.result(db.run(byteValues.schema.create), Duration.Inf)

  def saveFiles(files: Iterable[File]) = {
    val rows = files.map(file => (0, file.name, file.mime())).toList

    val run = db.run { filesWithId ++= rows }
    run.map(r => {
      r.zip(files).foreach(idAndFile => {
        val (fileId, file) = idAndFile
        if( fileId == 0 ) { println("ZEROOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOES")}
        file.id = fileId
        file
      })
    })
  }

  def saveProps(properties: Iterable[Tuple2[Int,FileProperties]]): Future[Unit] = {

    val rows = properties.flatMap(tuple => {
      val (fileId, props) = tuple
      props.getAll().map( (nameAndValue) => {
        val (name, value) = nameAndValue
        value match {
          case v: String => row(fileId, name, v)
          case v: Array[Byte] => row(fileId, name, v)
          case v: java.util.Date => row(fileId, name, v)
          case v: BigDecimal => row(fileId, name, v)
        }
      } )
    })

    db.run( DBIO.seq(rows.toSeq:_*) )
  }

  def close() = db.close()

  private def fileRow(name: String, mimetype: String) = filesWithId += (0, name, mimetype)
  private def row(fileId: Int, name: String, value: String) = stringValues += (fileId, name, value)
  private def row(fileId: Int, name: String, value: Array[Byte]) = byteValues += (fileId, name, value.asInstanceOf)
  private def row(fileId: Int, name: String, value: java.util.Date) = dateValues += (fileId, name, value.asInstanceOf)
  private def row(fileId: Int, name: String, value: BigDecimal) = numberValues += (fileId, name, value)

}