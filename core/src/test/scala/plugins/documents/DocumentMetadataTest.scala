package plugins.documents

import pl.dyskobol.prototype.plugins.document.flows.DocumentMetaDataExtract
import plugins.PluginTest

import scala.collection.mutable

class DocumentMetadataTest extends PluginTest{

  test("test pdf meta extract"){
    val imagePath = "./core/res/test.iso"
    val propsToFileMap: mutable.Map[String, Seq[(String, Any)]] = scala.collection.mutable.Map(
      "/DOCUMENTS/TEST_MARSZOWY_2KM.PDF" -> Seq(
        ("Creation-Date", "2013-10-06T10:32:05Z"),
        ("xmpMM:DocumentID", "446d467c-30ce-11e3-0000-dd09d950ac16"),
        ("access_permission:modify_annotations", "false"),
        ("pdf:docinfo:creator", "Kusy"),
        ("producer", "GPL Ghostscript  9.0")
      ),
    "/DOCUMENTS/EXAMPLE_RECORDS_FOR_REVALID.PDF" -> Seq(
        ("Creation-Date", "2017-04-26T14:04:00Z"),
        ("pdf:encrypted", "false"),
        ("access_permission:fill_in_form", "true"),
        ("creator", "Osama Ammar"),
        ("producer", "Microsoft® Word 2010")
      )

    )
    testFlowProps(imagePath, DocumentMetaDataExtract(), None, propsToFileMap)

  }

  test("test Ms office word 2007+ meta extract"){
    val imagePath = "./core/res/test.iso"
    val propsToFileMap: mutable.Map[String, Seq[(String, Any)]] = scala.collection.mutable.Map(
      "/DOCUMENTS/JOURNAL.PONE.0041015.S002.DOCX" -> Seq(
        ("Creation-Date", "2012-01-30T16:34:00Z"),
        ("dc:title", "The Trier Social Stress Test (TSST)"),
        ("Last-Author", "vrgassli")
      )
    )
    testFlowProps(imagePath, DocumentMetaDataExtract(), None, propsToFileMap)

  }

  test("test Ms office powerpoint 2007+ meta extract"){
    val imagePath = "./core/res/test.iso"
    val propsToFileMap: mutable.Map[String, Seq[(String, Any)]] = scala.collection.mutable.Map(
      "/DOCUMENTS/JOURNAL.PONE.0041015.S002.DOCX" -> Seq(
        ("Creation-Date", "2015-08-10T14:47:42Z"),
        ("dc:publisher", "National and Kapodistrian University of Athens"),
        ("Application-Name", "Microsoft Office PowerPoint")
      )
    )
    testFlowProps(imagePath, DocumentMetaDataExtract(), None, propsToFileMap)

  }

  test("test MS Office word meta extract"){
    val imagePath = "./core/res/test.iso"
    val propsToFileMap: mutable.Map[String, Seq[(String, Any)]] = scala.collection.mutable.Map(
      "/DOCUMENTS/TEST_CALCULIA.DOC" -> Seq(
        ("meta:save-date", "2012-05-17T21:47:00Z"),
        ("meta:last-author", "uzytkownik"),
        ("modified", "2012-05-17T21:47:00Z"),
        ("Application-Name", "Microsoft Office Word"),
        ("Word-Count", "4284")
      ),
      "/DOCUMENTS/UWAGI_O_STOSOWANIU_TESTU_70.DOC" -> Seq(
        ("meta:save-date", "2007-01-17T12:11:00Z"),
        ("meta:last-author", "Administrator"),
        ("modified", "2007-01-17T12:11:00Z"),
        ("Application-Name", "Microsoft Word 10.0"),
        ("Word-Count", "1484")

      ),
      "/DOCUMENTS/TESTCASEMATRIX.XLS" -> Seq(
        ("meta:save-date", "2007-06-13T07:42:38Z"),
        ("Last-Author", "Jari Vanhanen"),
        ("modified", "2007-06-13T07:42:38Z"),
        ("Application-Name", "Microsoft Excel"),
        ("Last-Printed", "2005-10-11T05:23:13Z")

      )
    )
    testFlowProps(imagePath, DocumentMetaDataExtract(), None, propsToFileMap)

  }

  test("test MS Office Excel meta extract"){
    val imagePath = "./core/res/test.iso"
    val propsToFileMap: mutable.Map[String, Seq[(String, Any)]] = scala.collection.mutable.Map(
      "/DOCUMENTS/TESTCASEMATRIX.XLS" -> Seq(
        ("meta:save-date", "2007-06-13T07:42:38Z"),
        ("Last-Author", "Jari Vanhanen"),
        ("modified", "2007-06-13T07:42:38Z"),
        ("Application-Name", "Microsoft Excel"),
        ("Last-Printed", "2005-10-11T05:23:13Z")
      )
    )
    testFlowProps(imagePath, DocumentMetaDataExtract(), None, propsToFileMap)

  }


}
