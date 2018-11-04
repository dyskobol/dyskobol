package pl.dyskobol

import com.typesafe.config.Config

package object examples {
  trait Process{
    def run(config: Config)
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
    "2" -> ("Application persising data in PostgresDB",PostgresDbApp),
    "3" -> ("Docs, file meta and basic info with unzipping",UnzipExampleApp),
    "4" -> ("Docs, file meta and basic info on worker pool",WorkerPoolApp),
    "5" -> ("Docs meta and content ",DocsApp),
    "6" -> ("Image meta and thumbnail ",ImageApp),
  )

}
