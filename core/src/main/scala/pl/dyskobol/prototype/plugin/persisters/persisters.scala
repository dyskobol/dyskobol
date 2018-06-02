package pl.dyskobol.prototype.plugin.persisters


import pl.dyskobol.prototype.stages.Persister

object Persisters {
  def oraclePersister(databaseUrl:String) = new Persister(databaseUrl)

}
