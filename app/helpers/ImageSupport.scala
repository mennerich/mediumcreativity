package helpers

import java.io.File
import java.util.UUID
import models.Image
import org.apache.commons.io.FilenameUtils

trait ImageSupport {

  def processImage(file: File): Image = {
    val uuid = UUID.randomUUID()
    val ext = FilenameUtils.getExtension(file.getName)
    val version = 1
    val managedFile = new File(s"public/images/${uuid.toString}.$ext")
    managedFile.createNewFile()
    new Image(0, version, uuid, ext)
  }

}
