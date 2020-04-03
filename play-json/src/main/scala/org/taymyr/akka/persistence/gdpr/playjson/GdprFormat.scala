package org.taymyr.akka.persistence.gdpr.playjson

import org.taymyr.akka.persistence.gdpr.WithDataSubjectId
import org.taymyr.akka.persistence.gdpr.internal.GdprSerializerAccess
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

import java.util.Base64

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/#akka.persistence.gdpr.playjson.GdprFormat$ GdprFormat]].
 */
object GdprFormat {
  def specificWithDataSubjectIdFormat[T <: AnyRef]: Format[WithDataSubjectId[T]] = format(deserialize, serialize)

  implicit val withDataSubjectIdFormat: Format[WithDataSubjectId[_]] = format(deserialize, serialize)

  private def format =
    (JsPath \ "dataSubjectId").format[String] and
      (JsPath \ "payload").format[String]

  private def deserialize[T <: AnyRef](dataSubjectId: String, encryptedPayload: String): WithDataSubjectId[T] =
    Option(encryptedPayload) match {
      case Some(value) =>
        val encryptedBytes = Base64.getDecoder.decode(value)
        val decryptedPayload = GdprSerializerAccess.fromBinary[T](encryptedBytes).payload
        WithDataSubjectId(dataSubjectId, decryptedPayload)
      case None =>
        WithDataSubjectId.shredded(dataSubjectId)
    }

  private def serialize(withDataSubjectId: WithDataSubjectId[_]): (String, String) = {
    val payload = Base64.getEncoder.encodeToString(GdprSerializerAccess.toBinary(withDataSubjectId))
    (withDataSubjectId.dataSubjectId, payload)
  }
}
