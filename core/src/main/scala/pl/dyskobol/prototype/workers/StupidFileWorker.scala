package pl.dyskobol.prototype.workers

import pl.dyskobol.model.File

trait StupidFileWorker extends AbstractFileWorker{
  override def canWork: Boolean = {
    scala.util.Random.nextBoolean()
  }

  override def work(file: File): Unit = {
    Thread.sleep(1000)
    log.info("Working hard...")

  }
}
