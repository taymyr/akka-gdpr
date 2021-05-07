package org.taymyr.akka.persistence.gdpr.javadsl

import akka.Done
import akka.actor.ExtendedActorSystem
import org.taymyr.akka.persistence.gdpr.scaladsl.{DelegatingKeyManagement, AbstractGdprEncryption => ScalaAbstractGdprEncryption, KeyManagement => ScalaKeyManagement}

import java.security.SecureRandom
import java.util.Optional
import java.util.concurrent.CompletionStage

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/#akka.persistence.gdpr.javadsl.AbstractGdprEncryption AbstractGdprEncryption]]
 */
abstract class AbstractGdprEncryption(system: ExtendedActorSystem) extends GdprEncryption {

  private lazy val delegate = {
    val impl = new ScalaAbstractGdprEncryption(system) {
      private val delegatingKeyManagement = new DelegatingKeyManagement(AbstractGdprEncryption.this.keyManagement())
      override def keyManagement(): ScalaKeyManagement = delegatingKeyManagement
    }
    new DelegatingGdprEncryption(impl)
  }

  protected def keyManagement(): KeyManagement

  protected def secureRandom: SecureRandom = new SecureRandom()

  override def decrypt(payload: Array[Byte], dataSubjectId: String): CompletionStage[Optional[Array[Byte]]] =
    delegate.decrypt(payload, dataSubjectId)

  override def encrypt(payload: Array[Byte], dataSubjectId: String): CompletionStage[Array[Byte]] =
    delegate.encrypt(payload, dataSubjectId)

  override def shred(dataSubjectId: String): CompletionStage[Done] =
    delegate.shred(dataSubjectId)
}
