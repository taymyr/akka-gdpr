package org.taymyr.akka.persistence.gdpr.scaladsl

import akka.actor.ExtendedActorSystem
import akka.serialization.SerializationExtension
import org.taymyr.akka.persistence.gdpr.GdprSerializer.WithDataSubjectIdManifest
import org.taymyr.akka.persistence.gdpr.{GdprSerializer, WithDataSubjectId}

import scala.concurrent.Future
import scala.concurrent.Future.failed
import scala.util.{Failure, Success}

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/#akka.persistence.gdpr.scaladsl.WithDataSubjectIdSerialization WithDataSubjectIdSerialization]]
 */
class WithDataSubjectIdSerialization(system: ExtendedActorSystem) {
  private lazy val gdprSerializer = SerializationExtension.get(system)
    .serializerOf(classOf[GdprSerializer].getName)
    .map(_.asInstanceOf[GdprSerializer])

  def fromBinaryAsync[T <: AnyRef](bytes: Array[Byte]): Future[WithDataSubjectId[T]] = gdprSerializer match {
    case Success(serializer) => serializer
      .fromBinaryAsync(bytes, WithDataSubjectIdManifest).asInstanceOf[Future[WithDataSubjectId[T]]]
    case Failure(exception) => failed(exception)
  }

  def toBinaryAsync(data: WithDataSubjectId[_]): Future[Array[Byte]] = gdprSerializer match {
    case Success(serializer) => serializer.toBinaryAsync(data)
    case Failure(exception) => failed(exception)
  }
}
