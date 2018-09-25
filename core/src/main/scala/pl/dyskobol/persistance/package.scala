package pl.dyskobol

import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.persistance.dbMap
import pl.dyskobol.prototype.persistance.DB
import ucar.nc2.ft.point.standard.CoordSysEvaluator.Predicate
import ucar.nc2.stream.NcStreamProto.DataType

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

package object persistance {
  type dbMap = Map[String, AnyRef]
  type action = Function2[Any, dbMap, Future[Any]]

  case class DatabaseNotExistingException(name: String) extends Exception
  case class ActionNotFound(data: Any) extends Exception
  case class SaveProps(props: List[(Int, FileProperties)])
  case class SaveFiles(files: List[File])

  val basicRepository: Repository = {
    val rep = new Repository

    // Persisting files
    rep.addCallback(
      {
        case SaveFiles(list) => true
        case _ => false
      },
      (data, dbs) => data match {
          case SaveFiles(files) =>
            val db = dbs.get("relational").asInstanceOf[Option[DB]]

            if( db.isEmpty ) throw DatabaseNotExistingException("relational")
            db.get.saveFiles(files)

      }
    )

    // Persisting file props
    rep.addCallback(
      {
        case SaveProps(list) => true
        case _ => false
      },
      (data, dbs) => data match {
        case SaveProps(props) =>
        val db = dbs.get("relational").asInstanceOf[Option[DB]]

        if( db.isEmpty ) throw DatabaseNotExistingException("relational")
        db.get.saveProps(props)
      }
    )

    rep
  }
}