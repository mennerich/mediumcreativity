package models

import java.util.UUID

import javax.inject.Inject
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Image(id: Int, version: Int, uuid: UUID, ext: String)

class ImageRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private[models] val images = TableQuery[ImagesTable]

  private[models] class ImagesTable(tag: Tag) extends Table[Image](tag, "work") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def version: Rep[Int] = column[Int]("version")
    def uuid: Rep[UUID] = column[UUID]("uuid")
    def ext: Rep[String] = column[String]("ext")
    def * = (id, version, uuid, ext) <> (Image.tupled, Image.unapply)
  }

  def all: Future[List[Image]] = db.run(images.to[List].result)

  def create(image: Image): Future[Int] = db.run(images += image)

}
