package controllers

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.inject.ApplicationLifecycle
import play.api.Configuration
import play.api.mvc._
import scala.concurrent.ExecutionContext

class AppController @Inject()
  (implicit ec: ExecutionContext, 
  config: Configuration,
  lifecycle: ApplicationLifecycle,
  val controllerComponents: ControllerComponents) 
  extends BaseController 
  with I18nSupport {

  def index = Action { Ok(views.html.index()) }

}