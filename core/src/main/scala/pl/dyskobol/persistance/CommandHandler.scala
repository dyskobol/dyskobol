package pl.dyskobol.persistance

import scala.concurrent.Future


class CommandHandler(implicit val repository: Repository, implicit val dbs: dbMap){
  def persist(data: Any): Future[Any] ={
    repository.persist(data, dbs)
  }
}