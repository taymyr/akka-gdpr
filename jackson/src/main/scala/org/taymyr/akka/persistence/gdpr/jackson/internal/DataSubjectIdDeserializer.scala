package org.taymyr.akka.persistence.gdpr.jackson.internal

import java.util.Arrays.asList
import java.util.Base64

import com.fasterxml.jackson.core.JsonToken.{END_OBJECT, VALUE_NULL}
import com.fasterxml.jackson.core.{JsonParseException, JsonParser}
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.databind.{DeserializationContext, JavaType}
import org.taymyr.akka.persistence.gdpr.WithDataSubjectId
import org.taymyr.akka.persistence.gdpr.internal.GdprSerializerAccess

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/jackson/internal/DataSubjectIdDeserializer.html DataSubjectIdDeserializer]]
 */
class DataSubjectIdDeserializer(javaType: JavaType) extends StdDeserializer[WithDataSubjectId[_]](javaType) with GdprSerializerAccess {

  override def deserialize(parser: JsonParser, context: DeserializationContext): WithDataSubjectId[_] = {
    var dataSubjectId: Option[String] = None
    var payload: Option[_ <: AnyRef] = None

    while (parser.nextToken() != END_OBJECT) {
      val field = parser.currentName
      parser.nextToken
      field match {
        case FieldNames.dataSubjectId =>
          dataSubjectId = readDataSubjectId(parser, context)
        case FieldNames.payload =>
          payload = readPayload(parser, context)
        case _ =>
          if (context.isEnabled(FAIL_ON_UNKNOWN_PROPERTIES))
            throw UnrecognizedPropertyException.from(
              parser,
              classOf[WithDataSubjectId[_]],
              field,
              asList(FieldNames.dataSubjectId, FieldNames.payload)
            )
          else
            parser.skipChildren
      }
    }

    // payload may be null when shredded
    new WithDataSubjectId(
      checkPresent(parser, dataSubjectId, FieldNames.dataSubjectId),
      payload
    )
  }

  private def readDataSubjectId(parser: JsonParser, context: DeserializationContext): Option[String] =
    if (parser.hasToken(VALUE_NULL)) None else Option(context.readValue(parser, classOf[String]))

  private def readPayload(parser: JsonParser, context: DeserializationContext): Option[_ <: AnyRef] = {
    if (parser.hasToken(VALUE_NULL))
      None
    else {
      val encryptedPayload = Base64.getDecoder.decode(context.readValue(parser, classOf[String]))
      Option(fromBinary[AnyRef](encryptedPayload)).flatMap[AnyRef](_.payload)
    }
  }

  private def checkPresent[T](parser: JsonParser, value: Option[T], name: String): T = {
    if (value.isEmpty) throw new JsonParseException(parser, s"Missing property: $name")
    value.get
  }
}


