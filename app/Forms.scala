package forms

import models.{Work, UserForm}
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

  val userForm = Form {
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "nick" -> nonEmptyText,
      "isAdmin" -> boolean
    ) (UserForm.apply)(UserForm.unapply)
  }

}




