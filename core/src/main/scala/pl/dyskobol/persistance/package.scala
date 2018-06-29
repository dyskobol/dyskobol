package pl.dyskobol

import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.persistance.DB

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

package object persistance {
  type dbMap = Map[String, AnyRef]
  type action = (Any, dbMap) => Future[Any]

  case class DatabaseNotExistingException(name: String) extends Exception

  val basicRepository: Repository = {
    val rep = new Repository

    // Persisting files
    rep.addCallback(classOf[List[File]], (data: Any, dbs: dbMap) => {
      val db = dbs.get("relational").asInstanceOf[Option[DB]]

      if( db.isEmpty ) throw DatabaseNotExistingException("relational")

      db.get.saveFiles(data.asInstanceOf[List[File]])
    })

    // Persisting file props
    rep.addCallback(classOf[List[(Int,FileProperties)]], (data: Any, dbs: dbMap) => {
      val db = dbs.get("relational").asInstanceOf[Option[DB]]

      if( db.isEmpty ) throw DatabaseNotExistingException("relational")

      db.get.saveProps(data.asInstanceOf[List[(Int,FileProperties)]])
    })
    rep
  }
}
