package plugins.image
import pl.dyskobol.prototype.plugins.image.flows.ImageMetaExtract
import _root_.plugins.PluginTest

import scala.collection.mutable

class ImagePluginTest extends PluginTest {

  test("test jpg extract"){
    val imagePath = "./core/res/test.iso"
    val propsToFileMap: mutable.Map[String, Seq[(String, Any)]] = scala.collection.mutable.Map(
      "/IMAGES/JPG/EXIF_ORG.JPG" -> Seq(("Model","'MX-1700ZOOM'")),
      "/IMAGES/JPG/GPS.JPG" -> Seq(
        ("GPSLongitude","11, 53, 742199999/100000000 (7.422)"),
        ("GPSLongitudeRef","'E'"),
        ("GPSLatitudeRef","'N'"),
        ("GPSDateStamp","'2008:10:23'")
      ),
      "/IMAGES/JPG/HDR.JPG" -> Seq(
        ("ExposureCompensation","0"),
        ("ExposureTime","1/200 (0.005)")
      ),
      "/IMAGES/JPG/INVALID.JPG" -> Seq(), //Empty props
       "/IMAGES/JPG/SONY_CYBERSHOT.JPG" -> Seq(("Make","'SONY'"))
    )
   testFlow(imagePath, ImageMetaExtract("image/jpeg"::Nil), 5, propsToFileMap)
  }

  test("test tiff extract"){
    val imagePath = "./core/res/test.iso"
    val propsToFileMap: mutable.Map[String, Seq[(String, Any)]] = scala.collection.mutable.Map(
      "/IMAGES/TIFF/ARBITRO.TIFF" -> Seq(
        ("SamplesPerPixel","4"),
        ("PreviewImageLength","6391"),
        ("PhotometricInterpretation","2")
      ),
      "/IMAGES/TIFF/BSG1.TIFF" -> Seq(
        ("SamplesPerPixel","4"),
        ("Orientation","1"),
        ("BitsPerSample","8, 8, 8, 8")
      ),
      "/IMAGES/TIFF/CR__MIEUX11.TIFF" -> Seq(
        ("SamplesPerPixel","4"),
        ("YResolution","72"),
        ("Artist","'Jean Cornillon'")
      )
      )

   testFlow(imagePath, ImageMetaExtract("image/tiff"::Nil), 3, propsToFileMap)
  }

}
