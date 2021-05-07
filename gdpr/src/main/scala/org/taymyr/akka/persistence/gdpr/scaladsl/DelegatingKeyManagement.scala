package org.taymyr.akka.persistence.gdpr.scaladsl

import akka.Done
import javax.crypto.SecretKey
import org.taymyr.akka.persistence.gdpr.javadsl.{KeyManagement => JavaKeyManagement}

import scala.concurrent.Future

/**
 * Custom implementation of [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/scaladsl/DelegatingKeyManagement.html DelegatingKeyManagement]]
 */
private[gdpr] class DelegatingKeyManagement(delegate: JavaKeyManagement) extends KeyManagement {
  import scala.compat.java8.FutureConverters._
  import scala.compat.java8.OptionConverters._

  override def getKey(dataSubjectId: String): Future[Option[SecretKey]] =
    delegate.getKey(dataSubjectId).thenApply[Option[SecretKey]](opt => opt.asScala).toScala

  override def getOrCreateKey(dataSubjectId: String): Future[SecretKey] =
    delegate.getOrCreateKey(dataSubjectId).toScala

  override def shred(dataSubjectId: String): Future[Done] =
    delegate.shred(dataSubjectId).toScala
}
