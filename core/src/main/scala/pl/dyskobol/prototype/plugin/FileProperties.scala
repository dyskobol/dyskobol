package pl.dyskobol.prototype.plugin

import java.util.Date

import pl.dyskobol.model.File

class FileProperties(val id: File) {
  lazy val stringValues: Map[String, String] = Map()
  lazy val numberValues: Map[String, BigDecimal] = Map()
  lazy val dateValues: Map[String, Date] = Map()


  def addProperty(name: String, value: String): Unit = {
    stringValues + (name -> value)
  }

  def addProperty(name: String, value: BigDecimal): Unit = {
    numberValues + (name -> value)
  }

  def addProperty(name: String, value: Date): Unit = {
    dateValues + (name -> value)
  }
}
