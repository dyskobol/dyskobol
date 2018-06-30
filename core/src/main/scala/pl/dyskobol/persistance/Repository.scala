package pl.dyskobol.persistance

import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.persistance.DB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.Future

class Repository {
  var persistingCallbacks: List[ (Any=>Boolean, action) ] = Nil

  def persist(data : Any, dbs: dbMap): Future[Any] = {
    val actions = persistingCallbacks.filter(_._1(data)).take(1)
    if( actions.isEmpty ) {
      throw ActionNotFound(data)
    }
    actions.head._2.apply(data, dbs)
  }


  def addCallback(predicate: Any=>Boolean, callback: action): Unit ={
    persistingCallbacks = (predicate, callback) +: persistingCallbacks
  }
}