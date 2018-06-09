package pl.dyskobol.prototype.persist

import slick.driver.PostgresDriver.api._
import akka.actor.Actor
import pl.dyskobol.prototype.persist.Main.{db, mimetypes}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent._
import ExecutionContext.Implicits.global

class PersistActor extends Actor{

  val db = Database.forURL("jdbc:postgresql://localhost/postgres?user=postgres&password=postgres")

  override def receive: Receive = {
    case BasicFileInfo(name, property, propertyValue) => {
      if( property == "mimetype"){
        try{
          Await.result(db.run(DBIO.seq(
            mimetypes += (1, f"${propertyValue}", f"${name}"),
            mimetypes.result.map(println))), Duration.Inf)
        } finally db.close
      }

      sender() ! InfoPersisted()
    }
  }
}
