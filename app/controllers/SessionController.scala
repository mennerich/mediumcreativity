package controllers

import forms.Forms._
import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.inject.ApplicationLifecycle
import play.api.mvc._
import scala.concurrent.ExecutionContext

class SessionController  @Inject()
  (implicit ec: ExecutionContext,
    config: Configuration,
    lifecycle: ApplicationLifecycle,
    val controllerComponents: ControllerComponents,
    workRepo: WorkRepo, imageRepo: ImageRepo, userRepo: UserRepo, sessionRepo: SessionRepo)

  extends BaseController
    with I18nSupport {

  def authenticate() = Action { implicit request =>
    authForm.bindFromRequest.fold(
      formWithErrors => {
        Redirect(routes.AppController.index())
      },
      user => {
        userRepo.authenticate(user.email, user.password) match {
          case Some(s) => {
            Redirect(routes.AdminController.index())
              .withSession("gallery-session" -> s
              //.flashing("success" -> "successfuly logged in")
            )
          }
          case None => Redirect(routes.AppController.index())
        }
      }
    )
  }
}

