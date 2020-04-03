package org.taymyr.akka.persistence.gdpr.jackson.internal

import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.{BeanDescription, DeserializationConfig, JavaType, JsonDeserializer}
import org.taymyr.akka.persistence.gdpr.WithDataSubjectId

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/jackson/internal/DataSubjectIdDeserializerResolver.html DataSubjectIdDeserializerResolver]]
 */
object DataSubjectIdDeserializerResolver extends Deserializers.Base {
  override def findBeanDeserializer(`type`: JavaType, config: DeserializationConfig, beanDesc: BeanDescription): JsonDeserializer[_] =
    if (classOf[WithDataSubjectId[_]].isAssignableFrom(`type`.getRawClass)) new DataSubjectIdDeserializer(`type`) else null
}
