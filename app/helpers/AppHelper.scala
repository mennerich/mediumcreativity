package helpers

import models._
import models.Gallery._
import play.api.Configuration

trait AppHelper {

  def mapImages(images: List[Image]): Map[Int, String] = {
    var map = Map[Int, String]()
    for(image <- images) {
      map = map + (image.workId -> s"${image.uuid}.${image.ext}")
    }
    map
  }

  def getGalleryConfig(config: Configuration): GalleryConfig = {

    new GalleryConfig(
      config.get[String]("gallery.title"),
      config.get[String]("gallery.about"),
      config.get[String]("gallery.description"),
      config.get[String]("gallery.instagram"),
      config.get[String]("gallery.twitter"),
      config.get[String]("gallery.facebook")
    )
  }
}
