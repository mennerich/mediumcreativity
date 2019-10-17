package models

import java.util.UUID

import javax.inject.Inject
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

case class Image(id: Int, workId: Int, version: Int, uuid: String, ext: String)
case class Dimension(id: Int, dimension: String)

class ImageRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private[models] val images = TableQuery[ImagesTable]

  private[models] class ImagesTable(tag: Tag) extends Table[Image](tag, "image") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def workId: Rep[Int] = column[Int]("work_id")
    def version: Rep[Int] = column[Int]("version")
    def uuid: Rep[String] = column[String]("uuid")
    def ext: Rep[String] = column[String]("ext")
    def * = (id, workId, version, uuid, ext) <> (Image.tupled, Image.unapply)
  }

  def all: Future[List[Image]] = db.run(images.to[List].result)

  def create(image: Image): Future[Int] = db.run(images += image)

  def findByWorkId(workId: Int): Future[Option[Image]] = db.run(images.filter(_.workId === workId).result.headOption)


  private[models] val dimensions = TableQuery[DimensionsTable]

  private[models] class DimensionsTable(tag: Tag) extends Table[Dimension](tag, "dimension") {
    def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def dimension: Rep[String] = column[String]("dimension")
    def * = (id, dimension) <> (Dimension.tupled, Dimension.unapply)
  }

  val defaultDimensions: Vector[String] = Vector[String](
    "2 in. square magnets",
    "3 in. square magnets",
    "3 in. round magnets",
    "5x7 in. canvas board",
    "5x7 in. stretched canvas",
    "8 in. round stretched canvas",
    "8x8 in. gesso board",
    "8x10 in. stretched canvas",
    "8x10 in. canvas board",
    "11x14 in. stretched canvas",
    "12x16 in. stretched canvas",
    "9x12 in. panel board",
    "12x12x12 in. triangle stretched canvas"
  )

  def populateDimensions : Unit = {
    defaultDimensions.foreach { d =>
      createDimension(new Dimension(0, d))
    }
  }

  def getDimensionsMap(): List[Tuple2[String, String]]= {
    var list = List[Tuple2[String, String]]()
    Await.result(allDimensions, 2.seconds).foreach { d =>

      list = list ::: List(d.id.toString() -> d.dimension)
    }
    list
  }

  def findDimensionById(id: Int): Future[Option[Dimension]] = {
    db.run(dimensions.filter(_.id === id).result.headOption)
  }

  def allDimensions: Future[List[Dimension]] = db.run(dimensions.to[List].result)

  def createDimension(dimension: Dimension): Future[Int] = db.run(dimensions += dimension)

  def findById(id: Int): Future[Option[Dimension]] = db.run(dimensions.filter(_.id === id).result.headOption)

  def delete(id: Int): Future[Int] = db.run(images.filter(_.id === id).delete)

}
