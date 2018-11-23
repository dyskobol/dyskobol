package pl.dyskobol

import com.typesafe.config.Config

package object examples {
  trait Process{
    def helpMessage: String = {
      return s"You can customize this process using this options:\n${
        configOptions.map(entry =>{s"--${entry._1}\t\t${entry._2}"}).mkString("\n")
      }\n--all\t\tuse all customizable options"
    }

    def run(config: Config)
    val configOptions: Map[String, String] = Map()
  }
  def introduce(): Unit ={
    println("Existing processes:")
    processes.toSeq.sortWith(_._1<_._1)foreach(e => {
      val (k, v) = e
      println(s"\t${k}: ${v._1}")
    })
  }
  val processes: Map[String, (String, Process)] = Map(
    "1" -> ("Docs, file meta and basic info processing with stats ",MetricsApp),
    "2" -> ("Docs, file meta and basic info with unzipping",UnzipExampleApp),
    "3" -> ("Docs meta and content ",DocsApp),
    "4" -> ("Image meta and thumbnail ",ImageApp),
    "5" -> ("Customizable processing",CustomizableApp),
  )

}
