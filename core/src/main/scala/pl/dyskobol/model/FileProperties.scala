package pl.dyskobol.model

import java.util.Date

import scala.collection.mutable

class FileProperties {
  def getCheckInTime(gatewaysKey: String): Option[Long] = {
    checkIns.get(gatewaysKey)
  }

  def checkIn(gatewaysKey: String, timeInNanos: Long) ={
    checkIns += (gatewaysKey -> timeInNanos)
  }


  var _fileId: Option[Long] = None
  lazy val stringValues: mutable.Map[String, String] = mutable.Map()
  lazy val numberValues: mutable.Map[String, BigDecimal] = mutable.Map()
  lazy val dateValues: mutable.Map[String, Date] = mutable.Map()
  lazy val byteValues:  mutable.Map[String, Array[Byte]] = mutable.Map()
  lazy val checkIns: mutable.Map[String, Long] = mutable.Map()

  def getAll() = stringValues.iterator ++ numberValues.iterator ++ dateValues.iterator ++ byteValues.iterator

  def get(k: String): Any = {
    if(stringValues.get(k).isDefined)
      return stringValues(k)

    if(numberValues.get(k).isDefined)
      return numberValues(k)

    if(dateValues.get(k).isDefined)
      return dateValues(k)

    return byteValues(k)
  }

  def addProperty(name: String, value: String): Unit = {
    stringValues += (name -> value)
  }

  def addProperty(name: String, value: BigDecimal): Unit = {
    numberValues += (name -> value)
  }

  def addProperty(name: String, value: Date): Unit = {
    dateValues += (name -> value)
  }

  def addProperty(name: String, value: Array[Byte]): Unit = {
    byteValues += (name -> value)
  }

  override def toString: String = {
    val mapper = (kv: (Object,Object)) => f"${kv._1.toString} ${kv._2.toString}"
    f"Props:\n" +
      (stringValues.toList.map( mapper ) ++
        numberValues.toList.map( mapper ) ++
        dateValues.toList.map( mapper ) mkString "\n")
  }

  def clear(): Unit = {
    stringValues.clear()
    numberValues.clear()
    dateValues.clear()
  }
}