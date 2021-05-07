package org.taymyr.akka.persistence.gdpr.scaladsl

import java.nio.ByteBuffer
import java.security.SecureRandom

import akka.Done
import akka.actor.ExtendedActorSystem
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import org.taymyr.akka.persistence.gdpr.GdprSettings

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/#akka.persistence.gdpr.scaladsl.AbstractGdprEncryption AbstractGdprEncryption]]
 */
abstract class AbstractGdprEncryption(system: ExtendedActorSystem) extends GdprEncryption {
  private val config = system.settings.config
  private implicit val executor: ExecutionContextExecutor = system.dispatcher

  private val _gdprSettings = GdprSettings(system)

  protected def keyManagement(): KeyManagement

  protected def secureRandom: SecureRandom = new SecureRandom()

  def gdprSettings: GdprSettings = _gdprSettings

  override def decrypt(payload: Array[Byte], dataSubjectId: String): Future[Option[Array[Byte]]] =
    keyManagement().getKey(dataSubjectId).map {
      case Some(secretKey) =>
        val byteBuffer = ByteBuffer.wrap(payload)
        val iv = new Array[Byte](secretKey.getEncoded.length)
        byteBuffer.get(iv)
        val cipherPayload = new Array[Byte](byteBuffer.remaining)
        byteBuffer.get(cipherPayload)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(gdprSettings.gcmTlen, iv))

        Some(cipher.doFinal(cipherPayload))
      case _ => None
    }

  override def encrypt(payload: Array[Byte], dataSubjectId: String): Future[Array[Byte]] =
    keyManagement().getOrCreateKey(dataSubjectId).map { key =>
      /*
        The payload is encrypted with AES in GCM mode with no padding.
        Initialization vectors are created for each new payload using a SecureRandom and are the same length as the chosen key.
      */
      val iv = new Array[Byte](key.getEncoded.length)
      secureRandom.nextBytes(iv)
      val parameterSpec = new GCMParameterSpec(gdprSettings.gcmTlen, iv)
      val cipher = Cipher.getInstance("AES/GCM/NoPadding")
      cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
      val cipherPayload = cipher.doFinal(payload)
      /* The Initialization vector is then stored at the start of the encrypted payload. */
      val byteBuffer = ByteBuffer.allocate(iv.length + cipherPayload.length)
      byteBuffer.put(iv)
      byteBuffer.put(cipherPayload)

      byteBuffer.array
    }

  override def shred(dataSubjectId: String): Future[Done] = keyManagement().shred(dataSubjectId)
}
