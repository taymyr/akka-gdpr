package org.taymyr.akka.persistence.gdpr.javadsl

import java.util.concurrent.CompletionStage

import akka.actor.ExtendedActorSystem
import org.taymyr.akka.persistence.gdpr.WithDataSubjectId
import org.taymyr.akka.persistence.gdpr.scaladsl.{WithDataSubjectIdSerialization => ScalaWithDataSubjectIdSerialization}

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/javadsl/WithDataSubjectIdSerialization.html WithDataSubjectIdSerialization]]
 */
final class WithDataSubjectIdSerialization(system: ExtendedActorSystem) {
  import scala.compat.java8.FutureConverters._

  private val delegate = new ScalaWithDataSubjectIdSerialization(system)

  def fromBinaryAsync[T <: AnyRef](payloadClass: Class[T], bytes: Array[Byte]): CompletionStage[WithDataSubjectId[T]] =
    delegate.fromBinaryAsync[T](bytes).toJava

  def toBinaryAsync(data: WithDataSubjectId[_]): CompletionStage[Array[Byte]] = delegate.toBinaryAsync(data).toJava
}
