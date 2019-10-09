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

class SessionController  @Inject()
  (implicit ec: ExecutionContext,
    config: Configuration,
    lifecycle: ApplicationLifecycle,
    val controllerComponents: ControllerComponents,
    workRepo: WorkRepo, imageRepo: ImageRepo, userRepo: UserRepo, sessionRepo: SessionRepo)

  extends BaseController
    with I18nSupport {


  def authenticate() = Action.async { implicit request =>
    authForm.bindFromRequest.fold(
      formWithErrors => {
        Future(Redirect(routes.AppController.index()))
      },
      user => {
        userRepo.authenticate(user.email, user.password) match {
          case Some(s) => {
            Future(Redirect(routes.AdminController.index())
              .withSession("gallery-session" -> s)
              //.flashing("success" -> "successfuly logged in")
            )
          }
          case None => Future(Redirect(routes.AppController.index()))
        }
      }
    )
  }
}

