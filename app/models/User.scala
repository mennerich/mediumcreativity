package models

import javax.inject.Inject
import org.apache.commons.codec.digest.DigestUtils
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Random


case class User(id: Int, email: String, hash: String, salt: String, nick: String, isAdmin: Boolean)
case class UserForm(email: String, password: String, nick: String, isAdmin: Boolean)

class UserRepo @Inject()(sessionRepo: SessionRepo, protected val dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private[models] val Users = TableQuery[UsersTable]

  private[models] class UsersTable(tag: Tag) extends Table[User](tag, "user") {

    def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
    def email: Rep[String] = column[String]("email")
    def hash: Rep[String] = column[String]("hash")
    def salt: Rep[String] = column[String]("salt")
    def nick: Rep[String] = column[String]("nick")
    def isAdmin: Rep[Boolean] = column[Boolean]("is_admin")
    def * = (id, email, hash, salt, nick, isAdmin) <> (User.tupled, User.unapply)

  }


  def all: Future[List[User]] = db.run(Users.to[List].result)

  private def _findById(id: Int): DBIO[Option[User]] = Users.filter(_.id === id).result.headOption

  def findById(id: Int): Future[Option[User]] = db.run(_findById(id))

  def findBySessionKey(sessionKey: String): Option[Int] = {
    val action = sessionRepo.findBySessionKey(sessionKey)
    val result = Await.result(action, Duration.Inf)

    result match {
      case Some(key) => Some(key.userId)
      case None => None
    }
  }

  def adminExists(): Boolean = {
    Await.result(db.run(Users.filter(_.isAdmin === true).to[List].result), 5.seconds).nonEmpty
  }



  def create(userForm: UserForm): Future[Int] = {
    val salt = Random.alphanumeric.take(10).mkString
    val hash = DigestUtils.md5Hex(userForm.password + salt)
    val entry = User(0, userForm.email, hash, salt, userForm.nick, userForm.isAdmin)
    db.run(Users returning Users.map(_.id) += entry)
  }

  def update(user: UserForm): Future[Int] = {
    val action = findByEmail(user.email)
    val id = Await.result(action, Duration.Inf)

    id match {
      case Some(usr) => {
        val salt = Random.alphanumeric.take(10).mkString
        val hash = DigestUtils.md5Hex(user.password + salt)
        val entry = User(usr.id, user.email, hash, salt, user.nick, usr.isAdmin)
        db.run(Users.update(entry))
      }
      case None => throw new Exception
    }
  }

  def findByEmail(email: String): Future[Option[User]] = db.run(Users.filter(_.email === email).result.headOption)

  def authenticate(email: String, password: String): Option[String] = {
    val action = findByEmail(email)
    val result = Await.result(action, Duration.Inf)
    result match {
      case Some(user) => {
        val hash = DigestUtils.md5Hex(password + user.salt)
        hash == user.hash match {
          case true =>  {
            val sessionKey = DigestUtils.md5Hex(Random.alphanumeric.take(10).mkString)
            sessionRepo.create(user.id, sessionKey)
            Some(sessionKey)
          }
          case false => None
        }
      }
      case None => None
    }
  }



}