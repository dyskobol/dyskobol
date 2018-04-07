package pl.dyskobol.prototype.workers.application


import pl.dyskobol.model.File
import pl.dyskobol.prototype.workers.AbstractFileWorker

trait StupidFileWorker extends AbstractFileWorker{
  override def canWork: Boolean = {
    scala.util.Random.nextBoolean()
  }

  override def work(file: File): Unit = {
    Thread.sleep(1000)
    log.info("Working hard...")

  }
}
