package pl.dyskobol.prototype.plugins

import java.lang.reflect.Field

import akka.NotUsed
import akka.stream.scaladsl.GraphDSL
import pl.dyskobol.model.{File, FileProperties, FlowElements}
import pl.dyskobol.prototype.stages.ForEach

package object file {

  object flows {
      def FileMetadataExtract(full:Boolean=true) = new FileMetadataExtract(full).flow()
  }

  object filters extends filters

  object foreaches extends foreaches {
    def fileMeta(full: Boolean = true) =
      full match {
        case true => ForEach((pair: (FlowElements)) => {
          val (file, prop) = pair
          file.getClass.getDeclaredFields.foreach((f: Field) => {
            val value = f.get(file).toString
            if (value forall Character.isDigit)
              prop.addProperty(f.getName, BigDecimal(value))
            else
              prop.addProperty(f.getName, value)
          }
          )
        })

        case false => ForEach((pair: (FlowElements)) => {
          val (file, prop) = pair
          prop.addProperty("size", file.size)
          prop.addProperty("path", file.path)
          prop.addProperty("name", file.name)
          prop.addProperty("mtype", file.mime)

        })
      }


  }

}
