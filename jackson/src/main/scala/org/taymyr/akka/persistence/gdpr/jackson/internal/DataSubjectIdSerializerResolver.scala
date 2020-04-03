package org.taymyr.akka.persistence.gdpr.jackson.internal

import com.fasterxml.jackson.databind.{BeanDescription, JavaType, JsonSerializer, SerializationConfig}
import com.fasterxml.jackson.databind.ser.Serializers
import org.taymyr.akka.persistence.gdpr.WithDataSubjectId

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/jackson/internal/DataSubjectIdSerializerResolver.html DataSubjectIdSerializerResolver]]
 */
object DataSubjectIdSerializerResolver extends Serializers.Base {
  override def findSerializer(config: SerializationConfig, `type`: JavaType, beanDesc: BeanDescription): JsonSerializer[_] =
    if (classOf[WithDataSubjectId[_]].isAssignableFrom(`type`.getRawClass)) new DataSubjectIdSerializer else null
}
