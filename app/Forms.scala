package forms

import models._
import play.api.data.Form
import play.api.data.Forms._

object Forms {
  val workForm = Form(
    mapping(
      "id" -> number,
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "creationDate" -> sqlDate,
      "available" -> boolean
    )(Work.apply)(Work.unapply))
}