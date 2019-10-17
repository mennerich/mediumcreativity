package controllers

import forms.Forms._
import models._
import java.io.File
import java.nio.file.Paths
import java.util.UUID

import helpers.AppHelper
import javax.inject.Inject
import org.apache.commons.io.FilenameUtils
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.inject.ApplicationLifecycle
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class AdminController @Inject()
  (implicit ec: ExecutionContext, config: Configuration, lifecycle: ApplicationLifecycle, val controllerComponents: ControllerComponents,
    workRepo: WorkRepo, imageRepo: ImageRepo, userRepo: UserRepo, sessionRepo: SessionRepo)

  extends BaseController with I18nSupport with AppHelper {

  private def checkSession(option: Option[String]): Boolean = {
    option match {
      case Some(sessionKey) => sessionRepo.keyExists(sessionKey)
      case None => false
    }
  }

  def index: Action[AnyContent] = Action { implicit request =>
    checkSession(request.session.get("gallery-session")) match {
      case true => Ok(views.html.admin.index());
      case false => InternalServerError("No Valid Session Key")
    }
  }


  def createWork: Action[AnyContent] = Action { implicit request =>
    checkSession(request.session.get("gallery-session")) match {
      case true => {
        val dimensions = imageRepo.getDimensionsMap
        Ok(views.html.admin.createWork(workForm, dimensions))
      }

      case false => InternalServerError("No Valid Session Key")
    }
  }

  def insertWork: Action[AnyContent] = Action { implicit request =>
    checkSession(request.session.get("gallery-session")) match {
      case true => {
        workForm.bindFromRequest.fold(
          formWithErrors => {
            Redirect(routes.AdminController.createWork()) //add error to flash
          },
          form => {
            val work = Work(form.id, form.title, form.description, form.creationDate, form.available, form.dimensionId)
            val workId = Await.result(workRepo.create(work), 5.seconds).id
            Redirect(routes.AdminController.uploadImage(workId))
          }
        )
      }
      case false => InternalServerError("No Valid Session Key")
    }
  }

  def uploadImage(workId: Int): Action[AnyContent] = Action { implicit request =>
    checkSession(request.session.get("gallery-session")) match {
      case true => {
        Await.result(workRepo.findById(workId), 5.seconds) match {
          case Some(work) => Ok(views.html.admin.upload(work))
          case None => InternalServerError("InternalServerError")
        }
      }
      case false => InternalServerError("Invalid Key")
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

  def insertAdmin: Action[AnyContent] = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Redirect(routes.AppController.index())
      },
      form => {
        val user = Await.result(userRepo.create(form), 5.seconds)
        imageRepo.populateDimensions
        Redirect(routes.AdminController.login())
      }
    )
  }

  def insertUser: Action[AnyContent] = Action { implicit request =>
    request.session.get("gallery-session").map { sessionKey =>
      sessionRepo.keyExists(sessionKey) match {
        case true => {
          userForm.bindFromRequest.fold(
            formWithErrors => {
              Redirect(routes.AppController.index())
            },
            form => {
              val user = Await.result(userRepo.create(form), 5.seconds)
              Redirect(routes.AdminController.index())
            }
          )
        }
        case false => InternalServerError("Invalid Key")
      }
    }.getOrElse {
      InternalServerError("No Session Key")
    }
  }

  def login() = Action { implicit request =>
    userRepo.adminExists() match {
      case true => Ok(views.html.admin.login(authForm))
      case false => Ok(views.html.admin.setup(userForm))
    }
  }

  def delete(workId: Int): Action[AnyContent] = Action { implicit request =>
    request.session.get("gallery-session").map { sessionKey =>
      sessionRepo.keyExists(sessionKey) match {
        case true =>  {
          val image = Await.result(imageRepo.findByWorkId(workId), 5.seconds).get
          val imageFile: File = new File("public/images/gallery/" + image.uuid + "." + image.ext)
          imageFile.delete()
          //delete image record
          imageRepo.delete(image.id)
          //delete image
          workRepo.delete(image.id)
          //delete work
          Redirect(routes.AdminController.index)
        }
        case false => InternalServerError("Invalid Key")
      }
    }.getOrElse {
      InternalServerError("No Session Key")
    }
  }
  def deleteWork: Action[AnyContent] = Action { implicit request =>
    request.session.get("gallery-session").map { sessionKey =>
      sessionRepo.keyExists(sessionKey) match {
        case true => {
          val works = Await.result(workRepo.all, 5.seconds)
          val dimensions = Await.result(imageRepo.allDimensions, 5.seconds)
          val images = Await.result(imageRepo.all, 2.seconds)
          val imageMap = mapImages(images)
          Ok(views.html.admin.deleteWork(works, images, dimensions, imageMap))
        }
        case false => InternalServerError("Invalid Key")
      }
    }.getOrElse {
      InternalServerError("No Session Key")
    }
  }

}

