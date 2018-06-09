package pl.dyskobol.prototype.persist

import akka.persistence._
import pl.dyskobol.prototype.persist.FileEntity.Persist

object FileEntity{
  sealed trait Command

  final case class Persist(fileInfo: BasicFileInfo)
    extends Command

  sealed trait Event {
    val FileDescriptor : Int
    val filename : String
  }

  final case class Persisted(FileDescriptor : Int, filename : String)
    extends Event

  final case class NoSuchFile(FileDescriptor : Int)
    extends RuntimeException("No file with that descriptor!")

}


class FileEntity extends PersistentActor {
  override def receiveRecover: Receive = {
    case event: FileEntity.Event =>
      //state += event
//    case SnapshotOffer(_, snapshot: State) =>
//      state = snapshot
  }

  def state(fileInfo: BasicFileInfo): Any = ???

  override def receiveCommand: Receive = {
    case Persist(fileInfo) =>
      sender() ! state(fileInfo)
  }



  override def persistenceId: String = "file"
}
