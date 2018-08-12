package pl.dyskobol.model

import java.util.Date

import scala.collection.mutable

class FileProperties(
  val stringValues: mutable.MutableList[(String, String)] = mutable.MutableList(),
  val numberValues: mutable.MutableList[(String, BigDecimal)] = mutable.MutableList(),
  val dateValues: mutable.MutableList[(String, Date)] = mutable.MutableList(),
  val byteValues: mutable.MutableList[(String, Array[Byte])] = mutable.MutableList(),
  var _fileId: Option[Long] = None ) {


  def getAll() = stringValues.iterator ++ numberValues.iterator ++ dateValues.iterator ++ byteValues.iterator


  def addProperty(name: String, value: String): Unit = {
    stringValues += ((name, value))
  }

  def addProperty(name: String, value: BigDecimal): Unit = {
    numberValues += ((name, value))
  }

  def addProperty(name: String, value: Date): Unit = {
    dateValues += ((name, value))
  }

  def addProperty(name: String, value: Array[Byte]): Unit = {
    byteValues += ((name, value))
  }

  override def toString: String = {
    val mapper = (kv: (Object,Object)) => f"${kv._1.toString} ${kv._2.toString}"
    f"Props:\n" +
      (stringValues.map( mapper ) ++
        numberValues.map( mapper ) ++
        dateValues.map( mapper ) mkString "\n")
  }

  def clear(): Unit = {
    stringValues.clear()
    numberValues.clear()
    dateValues.clear()
  }

  def deepClone(): FileProperties ={
    new FileProperties(
     this.stringValues,
     this.numberValues,
     this.dateValues,
     this.byteValues,
      this._fileId)
  }
}