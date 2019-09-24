package controllers

import javax.inject.Inject
import models._
import play.api.i18n.I18nSupport
import play.api.inject.ApplicationLifecycle
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class AppController @Inject()
  (implicit ec: ExecutionContext, 
  config: Configuration,
  lifecycle: ApplicationLifecycle,
  val controllerComponents: ControllerComponents,
   workRepo: WorkRepo)
  extends BaseController 
  with I18nSupport {

  def index = Action.async { implicit request =>
    val works = Await.result(workRepo.all, 2.seconds)
    Future(Ok(views.html.index(works.size)))
  }

}