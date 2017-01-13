package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

/**
  * The user object.
  *
  * @param name      The unique name of the user.
  * @param loginInfo The linked login info.
  */
case class User(name: String,
                loginInfo: LoginInfo,
                activated: Boolean) extends Identity

