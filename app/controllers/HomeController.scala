package controllers

import javax.inject.Inject
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.I18nSupport
import play.api.inject.ApplicationLifecycle
import play.api.Configuration
import play.api.Logger
import play.api.mvc._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration

class HomeController @Inject()
  (implicit ec: ExecutionContext, 
  config: Configuration,
  lifecycle: ApplicationLifecycle,
  val controllerComponents: ControllerComponents) 
  extends BaseController 
  with I18nSupport {

  def index = Action { Ok("mediumcreativity.com") }

}