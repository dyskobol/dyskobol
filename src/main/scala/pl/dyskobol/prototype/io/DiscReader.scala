package pl.dyskobol.prototype.io
import java.util
import java.util.UUID

import org.sleuthkit.datamodel._

class DiscReader(var vfs_path: String) {
  def list_disk(): Unit = {
    var skCase = SleuthkitCase.newCase(vfs_path+".db")
    var process = skCase.makeAddImageProcess("", true, false, "")
    var paths = new util.ArrayList[String]
    paths.add(vfs_path)
    process.run(UUID.randomUUID().toString(), paths.toArray(new Array[String](paths.size())))
    process.commit()
    val images : util.List[Image] = skCase.getImages()
    print(images.get(0).getChildren())

  }

}
