package pl.dyskobol.prototype

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import pl.dyskobol.model.File
package object stages {
  def FileSource(path: String): Source[File, NotUsed] = Source.fromGraph(new FileReaderGraph(path))

  val FileTypeResolver: Flow[File, File, NotUsed] = Flow[File].map(f=> {f.mime(); f})
}
