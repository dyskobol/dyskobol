package plugins.documents

import pl.dyskobol.prototype.plugins.document.flows.DocumentContentExtract
import plugins.PluginTest

import scala.collection.mutable

class DocumentContentTest extends PluginTest {
  test("test pdf meta extract"){
    val imagePath = "./core/res/test.iso"
    val contentChunkToFile: mutable.Map[String, String] = scala.collection.mutable.Map(
      "/DOCUMENTS/TEST_MARSZOWY_2KM.PDF" -> "Co mierzy test: Ocenia sprawność krążeniowo-oddechową.",
      "/DOCUMENTS/EXAMPLE_RECORDS_FOR_REVALID.PDF" -> "Inhaler technique affects the fate of the inhaled drug. Learning how inspiratory flow can affect \ndrug delivery for different inhalers has helped me to help patients improve their inhaler technique \nand had a direct impact on improving their care."
      )

    testFlowContent(imagePath, DocumentContentExtract(), None, contentChunkToFile)

  }


  test("test MS Office word meta extract"){
    val imagePath = "./core/res/test.iso"
    val contentChunkToFile: mutable.Map[String, String] = scala.collection.mutable.Map(
      "/DOCUMENTS/TEST_CALCULIA.DOC" -> "Interpretację ilościową testu Kalkulia przeprowadzamy na podstawie czterech tabel.",
      "/DOCUMENTS/UWAGI_O_STOSOWANIU_TESTU_70.DOC" -> " praktyce spotkać się możemy z trzema głównymi rodzajami testów."
    )
    testFlowContent(imagePath, DocumentContentExtract(), None, contentChunkToFile)

  }

  test("test MS Office Excel meta extract"){
    val imagePath = "./core/res/test.iso"
    val contentChunkToFile: mutable.Map[String, String] = scala.collection.mutable.Map(
      "/DOCUMENTS/TESTCASEMATRIX.XLS" -> "Project X Priority levels Description"
    )

    testFlowContent(imagePath, DocumentContentExtract(), None, contentChunkToFile)

  }
}
