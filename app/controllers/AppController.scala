package controllers


import helpers.AppHelper
import javax.inject.Inject
import models._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.inject.ApplicationLifecycle
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class AppController @Inject()
  (implicit ec: ExecutionContext,
  config: Configuration,
  lifecycle: ApplicationLifecycle,
  val controllerComponents: ControllerComponents,
  workRepo: WorkRepo, imageRepo: ImageRepo, sessionRepo: SessionRepo, userRepo: UserRepo)

  extends BaseController

  with I18nSupport with AppHelper {

  lifecycle.addStopHook { () =>
    Future.successful(sessionRepo.deleteAll)
  }

  def index: Action[AnyContent]  = Action { implicit request =>
    val works = Await.result(workRepo.all, 2.seconds)
    val images = Await.result(imageRepo.all, 2.seconds)
    val imageMap = mapImages(images)
    Ok(views.html.index(works, imageMap))
  }

  def show(id: Int): Action[AnyContent] = Action { implicit request =>
    Await.result(workRepo.findById(id), 5.seconds) match {
      case Some(work) => {
        val image: Option[Image] = Await.result(imageRepo.findByWorkId(work.id), 5.seconds)
        val dimensions: Option[Dimension] = Await.result(imageRepo.findDimensionById(work.dimensionId), 5.seconds)
        Ok(views.html.show(work, image, dimensions))
      }
      case _ => InternalServerError("Work Not Found")
    }
  }

}