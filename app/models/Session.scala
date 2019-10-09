package models

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import org.apache.commons.codec.digest.DigestUtils
import slick.dbio
import slick.dbio.Effect.Read
import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.util.Random

case class AuthRequest(email: String, password: String)

case class SessionKey(id: Int, sessionKey: String, userId: Int)

class SessionRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._
  private[models] val SessionKeys = TableQuery[SessionKeysTable]

  def all: Future[List[SessionKey]] = db.run(SessionKeys.to[List].result)

  def findById(id: Int): Future[Option[SessionKey]] = db.run(_findById(id))

  private def _findById(id: Int): DBIO[Option[SessionKey]] = SessionKeys.filter(_.id === id).result.headOption

  def findBySessionKey(sessionKey: String): Future[Option[SessionKey]] = db.run(_findBySessionKey(sessionKey))

  private def _findBySessionKey(sessionKey: String): DBIO[Option[SessionKey]] = SessionKeys.filter(_.sessionKey === sessionKey).result.headOption

  def keyExists(sessionKey: String): Boolean = {
    val action = db.run(_findBySessionKey(sessionKey))
    val result = Await.result(action, Duration.Inf)
    result match {
      case Some(s) => true
      case None => false
    }
  }

  def create(userId: Int, key: String): Future[Int] = {
    val sessionKey = SessionKey(0, key, userId)
    db.run(SessionKeys returning SessionKeys.map(_.id) += sessionKey)
  }

  def delete(sessionKey: String): Future[Unit] = db.run(SessionKeys.filter(_.sessionKey === sessionKey).delete).map(_ => ())

  def deleteAll(): Unit = {
    def action = db.run(SessionKeys.delete)
    Await.result(action, Duration.Inf)
  }

  private[models] class SessionKeysTable(tag: Tag) extends Table[SessionKey](tag, "SESSION") {

    def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
    def sessionKey = column[String]("SESSION_KEY")
    def userId = column[Int]("USER_ID")
    def * = (id, sessionKey, userId) <> (SessionKey.tupled, SessionKey.unapply)
  }

}
