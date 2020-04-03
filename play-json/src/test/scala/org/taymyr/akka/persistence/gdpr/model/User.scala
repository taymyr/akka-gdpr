package org.taymyr.akka.persistence.gdpr.model

import org.taymyr.akka.persistence.gdpr.WithDataSubjectId
import org.taymyr.akka.persistence.gdpr.playjson.GdprFormat
import play.api.libs.json._

case class User(login: String, identityCard: WithDataSubjectId[IdentityCard])

object User {
  implicit val identityCardFormat = GdprFormat.specificWithDataSubjectIdFormat[IdentityCard]
  implicit val format = Json.format[User]
}
