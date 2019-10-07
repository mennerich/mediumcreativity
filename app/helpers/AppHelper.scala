package helpers

import models.Image

trait AppHelper {

  def mapImages(images: List[Image]): Map[Int, String] = {
    var map = Map[Int, String]()
    for(image <- images) {
      map = map + (image.workId -> s"${image.uuid}.${image.ext}")
    }
    map
  }
}
