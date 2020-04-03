package org.taymyr.akka.persistence.gdpr.javadsl

import java.util.Optional
import java.util.concurrent.CompletionStage

import akka.Done
import org.taymyr.akka.persistence.gdpr.scaladsl.{GdprEncryption => ScalaGdprEncryption}

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/javadsl/DelegatingGdprEncryption.html DelegatingGdprEncryption]]
 */
private[gdpr] class DelegatingGdprEncryption(delegate: ScalaGdprEncryption) extends GdprEncryption {
  import scala.compat.java8.FutureConverters._
  import scala.compat.java8.OptionConverters._

  override def decrypt(payload: Array[Byte], dataSubjectId: String): CompletionStage[Optional[Array[Byte]]] =
    delegate.decrypt(payload, dataSubjectId).toJava.thenApply[Optional[Array[Byte]]](opt => opt.asJava)

  override def encrypt(payload: Array[Byte], dataSubjectId: String): CompletionStage[Array[Byte]] =
    delegate.encrypt(payload, dataSubjectId).toJava

  override def shred(dataSubjectId: String): CompletionStage[Done] =
    delegate.shred(dataSubjectId).toJava
}
