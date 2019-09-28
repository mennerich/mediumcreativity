package controllers

import forms.Forms._
import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.inject.ApplicationLifecycle
import play.api.mvc.{Action, _}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class AdminController @Inject()
  (implicit ec: ExecutionContext,
  config: Configuration,
  lifecycle: ApplicationLifecycle,
  val controllerComponents: ControllerComponents,
  workRepo: WorkRepo)
  extends BaseController with I18nSupport {

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.admin.index())
  }

  def createWork: Action[AnyContent] = Action { implicit request => Ok(views.html.admin.createWork(workForm)) }

  def submitWork: Action[AnyContent] = Action.async { implicit request =>
    workForm.bindFromRequest.fold(
      formWithErrors => {
        Future(Redirect(routes.AdminController.createWork())) //add error to flash
      },
      form => {
        val work = Work(form.id, form.title, form.description, form.creationDate, form.available)
        workRepo.create(work).map(_ => Redirect(routes.AppController.index()))

      }
    )
  }
}

