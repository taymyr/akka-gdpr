package org.taymyr.akka.persistence.gdpr.scaladsl

import akka.Done
import org.taymyr.akka.persistence.gdpr.javadsl.{GdprEncryption => JavaGdprEncryption}

import scala.concurrent.Future

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/scaladsl/DelegatingGdprEncryption.html DelegatingGdprEncryption]]
 */
private[gdpr] class DelegatingGdprEncryption(delegate: JavaGdprEncryption) extends GdprEncryption {
  import scala.compat.java8.FutureConverters._
  import scala.compat.java8.OptionConverters._

  override def decrypt(payload: Array[Byte], dataSubjectId: String): Future[Option[Array[Byte]]] =
    delegate.decrypt(payload, dataSubjectId).thenApply[Option[Array[Byte]]](v => v.asScala).toScala

  override def encrypt(payload: Array[Byte], dataSubjectId: String): Future[Array[Byte]] =
    delegate.encrypt(payload, dataSubjectId).toScala

  override def shred(dataSubjectId: String): Future[Done] =
    delegate.shred(dataSubjectId).toScala
}
