package forms

import models.{Work, UserForm, AuthRequest}
import play.api.data.Form
import play.api.data.Forms._

object Forms {

  val workForm = Form(
    mapping(
      "id" -> number,
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "creationDate" -> sqlDate,
      "available" -> boolean,
      "dimensionId" -> number
    )(Work.apply)(Work.unapply))

  val userForm = Form {
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "nick" -> nonEmptyText,
      "isAdmin" -> boolean
    ) (UserForm.apply)(UserForm.unapply)
  }

  val authForm = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText)
    (AuthRequest.apply)(AuthRequest.unapply)
  )

}




