package org.taymyr.akka.persistence.gdpr.internal

import akka.actor.ActorSystem
import akka.serialization.{Serialization, SerializationExtension}
import org.taymyr.akka.persistence.gdpr.GdprSerializer.WithDataSubjectIdManifest
import org.taymyr.akka.persistence.gdpr.{GdprSerializer, WithDataSubjectId}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/internal/GdprSerializerAccess.html GdprSerializerAccess]]
 */
private[gdpr] trait GdprSerializerAccess {
  private lazy val serializer = SerializationExtension.get(currentSystem)
    .serializerOf(classOf[GdprSerializer].getName).get.asInstanceOf[GdprSerializer]

  def currentSystem: ActorSystem = Serialization.getCurrentTransportInformation.system

  def gdprSerializer: GdprSerializer = serializer

  def fromBinary[T <: AnyRef](bytes: Array[Byte]): WithDataSubjectId[T] = {
    val result = gdprSerializer.fromBinaryAsync(bytes, WithDataSubjectIdManifest)
    Await.result(result, Duration.Inf).asInstanceOf[WithDataSubjectId[T]]
  }

  def toBinary(data: WithDataSubjectId[_]): Array[Byte] = {
    val result = gdprSerializer.toBinaryAsync(data)
    Await.result(result, Duration.Inf)
  }
}

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/internal/GdprSerializerAccess$.html GdprSerializerAccess]]
 */
object GdprSerializerAccess extends GdprSerializerAccess
