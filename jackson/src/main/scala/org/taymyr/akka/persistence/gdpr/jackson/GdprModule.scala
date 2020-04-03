package org.taymyr.akka.persistence.gdpr.jackson

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.module.SimpleModule
import org.taymyr.akka.persistence.gdpr.jackson.internal.{DataSubjectIdDeserializerResolver, DataSubjectIdSerializerResolver}
/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/index.html#akka.persistence.gdpr.jackson.GdprModule GdprModule]]
 */
class GdprModule extends SimpleModule {
  override def setupModule(context: Module.SetupContext): Unit = {
    super.setupModule(context)
    context.addSerializers(DataSubjectIdSerializerResolver)
    context.addDeserializers(DataSubjectIdDeserializerResolver)
  }
}

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/index.html#akka.persistence.gdpr.jackson.GdprModule$ GdprModule]]
 */
object GdprModule extends GdprModule
