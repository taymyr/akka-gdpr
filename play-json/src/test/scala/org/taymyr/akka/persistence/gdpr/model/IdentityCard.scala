package org.taymyr.akka.persistence.gdpr.model

import play.api.libs.json._

case class IdentityCard(firstName: String, lastName: String, series: String, number: String)

object IdentityCard {
  implicit val format = Json.format[IdentityCard]
}
