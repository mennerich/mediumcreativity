package models

import java.sql.Date

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future



case class Work(id: Int, title: String, description: String, creationDate: Date, available: Boolean)

class WorkRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {


  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private[models] val Works = TableQuery[WorksTable]

  private[models] class WorksTable(tag: Tag) extends Table[Work](tag, "work") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def title = column[String]("title")
    def description = column[String]("description")
    def creationDate = column[Date]("creation_date")
    def available = column[Boolean]("available")
    def * = (id, title, description, creationDate, available) <> (Work.tupled, Work.unapply)
  }

  def all: Future[List[Work]] = db.run(Works.to[List].result)

}
