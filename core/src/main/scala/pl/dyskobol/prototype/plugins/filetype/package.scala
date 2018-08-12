package pl.dyskobol.prototype.plugins

import java.io.{IOException, InputStream}

import akka.NotUsed
import akka.stream.scaladsl.Flow
import org.apache.tika.Tika
import pl.dyskobol.model.FlowElements

package object filetype {
  object flows {
    val resolver: Flow[FlowElements, FlowElements, NotUsed] = {
      val tika = new Tika()

      Flow[FlowElements].map(fe => {
        val (file, _) = fe

        var mime = tika.detect(file.name)

        // Type not resolved properly
        if( mime == "application/octet-stream" ) {
          val stream = file.createStream
          try {
            mime = tika.detect(stream, file.name)
          } catch {
            // Something went wrong during reading file, we can just ignore it
            case _: IOException =>
          }
        }

        file.mime = mime
        fe
      })
    }
  }
}
