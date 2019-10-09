package controllers

import java.io.File
import java.nio.file.Paths
import java.util.UUID

import forms.Forms._
import javax.inject.Inject
import models._
import org.apache.commons.io.FilenameUtils
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.inject.ApplicationLifecycle
import play.api.mvc.{Action, _}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class AdminController @Inject()
  (implicit ec: ExecutionContext, config: Configuration, lifecycle: ApplicationLifecycle, val controllerComponents: ControllerComponents,
    workRepo: WorkRepo, imageRepo: ImageRepo, userRepo: UserRepo, sessionRepo: SessionRepo
  )

  extends BaseController
  with I18nSupport {

  def index: Action[AnyContent] = Action { implicit request => Ok(views.html.admin.index()) }

  def createWork: Action[AnyContent] = Action { implicit request => Ok(views.html.admin.createWork(workForm)) }

  def insertWork: Action[AnyContent] = Action { implicit request =>
    workForm.bindFromRequest.fold(
      formWithErrors => {
        Redirect(routes.AdminController.createWork()) //add error to flash
      },
      form => {
        val work = Work(form.id, form.title, form.description, form.creationDate, form.available)
        val workId = Await.result(workRepo.create(work), 5.seconds).id
        Redirect(routes.AdminController.uploadImage(workId))
      }
    )
  }

  def uploadImage(workId: Int): Action[AnyContent] = Action { implicit request =>
    Await.result(workRepo.findById(workId), 5.seconds) match {
      case Some(work) => Ok(views.html.admin.upload(work))
      case None => InternalServerError("InternalServerError")
    }
  }

  def upload = Action(parse.multipartFormData) { request =>
    val workId = request.body.asFormUrlEncoded("work_id")(0).toInt

    request.body
      .file("picture")
      .map { picture =>
        val ext = FilenameUtils.getExtension(Paths.get(picture.filename).getFileName.toString)
        val uuid = UUID.randomUUID().toString
        val loc = new File(s"public/images/gallery/$uuid.$ext")
        picture.ref.moveTo(Paths.get(loc.getCanonicalPath), replace = true)
        val image = new Image(0, workId, 1, uuid, ext)
        Await.result(imageRepo.create(image), 5.seconds)
        Redirect(routes.AppController.show(workId))
      }
      .getOrElse { InternalServerError("Internal Server Error") }
  }

  def setup: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.admin.setup(userForm))
  }

  def insertUser: Action[AnyContent] = Action { implicit request =>
    request.session.get("gallery-session").map { sessionKey =>
      sessionRepo.keyExists(sessionKey) match {
        userForm.bindFromRequest.fold(
          formWithErrors => {
            Redirect(routes.AppController.index())
          },
          form => {
            val user = Await.result(userRepo.create(form), 5.seconds)
            Redirect(routes.AdminController.index())


      def login() = Action { implicit request =>
        Ok(views.html.admin.login(authForm))
      }

}

