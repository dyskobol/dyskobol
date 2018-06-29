package pl.dyskobol.persistance

import pl.dyskobol.model.{File, FileProperties}
import pl.dyskobol.prototype.persistance.DB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable
import scala.concurrent.Future

class Repository {
  var persistingCallbacks: Map[Class[_], action] = Map()

  def persist(data : Any, dbs: dbMap): Future[Any] = {
    println(persistingCallbacks)
    println(data, data.getClass)
    persistingCallbacks.get(data.getClass).get(data, dbs)
  }


  def addCallback(key: Class[_], callback: action): Unit ={
    persistingCallbacks = persistingCallbacks + (key -> callback)
  }
}
