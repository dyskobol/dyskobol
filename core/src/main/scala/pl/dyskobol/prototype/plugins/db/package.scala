package pl.dyskobol.prototype.plugins


import akka.stream.scaladsl.Flow
import pl.dyskobol.persistance.CommandHandler


package object db {
  object flows {
    def SaveFile(bufferSize: Int = 100)(implicit commandHandler: CommandHandler) = Flow.fromGraph(new SaveFileGraph(bufferSize)(commandHandler))
  }
}