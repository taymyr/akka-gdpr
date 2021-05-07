package org.taymyr.akka.persistence.gdpr.jackson.internal

import java.util.Base64

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.taymyr.akka.persistence.gdpr.WithDataSubjectId
import org.taymyr.akka.persistence.gdpr.internal.GdprSerializerAccess

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/jackson/internal/DataSubjectIdSerializer.html DataSubjectIdSerializer]]
 */
class DataSubjectIdSerializer
  extends StdSerializer[WithDataSubjectId[_]](classOf[WithDataSubjectId[_]], true) with GdprSerializerAccess {

  override def serialize(value: WithDataSubjectId[_], gen: JsonGenerator, provider: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeObjectField(FieldNames.dataSubjectId, value.dataSubjectId)
    gen.writeObjectField(FieldNames.payload, Base64.getEncoder.encodeToString(toBinary(value)))
    gen.writeEndObject()
  }
}
