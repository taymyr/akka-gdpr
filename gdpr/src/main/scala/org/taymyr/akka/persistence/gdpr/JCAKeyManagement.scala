package org.taymyr.akka.persistence.gdpr

import akka.Done
import akka.actor.ExtendedActorSystem
import org.taymyr.akka.persistence.gdpr.JCASettings.ValidKeyStores
import org.taymyr.akka.persistence.gdpr.scaladsl.KeyManagement

import java.io.{File, FileInputStream, FileOutputStream}
import java.security.KeyStore
import javax.crypto.spec.SecretKeySpec
import javax.crypto.{KeyGenerator, SecretKey}
import scala.concurrent.{ExecutionContext, Future, blocking}

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/JCAKeyManagement.html JCAKeyManagement]].
 */
class JCAKeyManagement(system: ExtendedActorSystem, gdprSettings: GdprSettings, jcaSettings: JCASettings) extends KeyManagement {
  private val algorithm = "AES"
  private val keyRecoveryPassword = jcaSettings.password.toCharArray // assume the same password as for the keystore
  private val keyStoreFile = new File(jcaSettings.location)
  private val keyStorePassword = jcaSettings.password.toCharArray
  private implicit val ec: ExecutionContext = system.dispatchers.lookup(jcaSettings.dispatcher)

  private val keyStore: KeyStore = {
    val ksType = ValidKeyStores.find(_.equalsIgnoreCase(jcaSettings.keystoreType)) match {
      case Some(value) => value
      case None => throw new IllegalArgumentException(s"Key store type ${jcaSettings.keystoreType} is not supported")
    }
    val ks = KeyStore.getInstance(ksType)
    closing(new FileInputStream(keyStoreFile)) { ks.load(_, keyStorePassword) }
    ks
  }
  override def getKey(dataSubjectId: String): Future[Option[SecretKey]] = Future {
    blocking {
      if (!keyStore.containsAlias(dataSubjectId)) {
        closing(new FileInputStream(keyStoreFile)) { keyStore.load(_, keyStorePassword) }
      }
      Option(keyStore.getKey(dataSubjectId, keyRecoveryPassword)).map(key => new SecretKeySpec(key.getEncoded, key.getAlgorithm))
    }
  }

  override def getOrCreateKey(dataSubjectId: String): Future[SecretKey] = getKey(dataSubjectId).map {
    case Some(secretKey) => secretKey
    case None => blocking {
      val secretKey = generateSecretKey()
      keyStore.setEntry(
        dataSubjectId,
        new KeyStore.SecretKeyEntry(secretKey),
        new KeyStore.PasswordProtection(keyRecoveryPassword)
      )
      storeAndLoadKeyStore()
      secretKey
    }
  }

  private def generateSecretKey(): SecretKey = {
    val keyGen = KeyGenerator.getInstance(algorithm)
    keyGen.init(gdprSettings.keySize)
    keyGen.generateKey
  }

  override def shred(dataSubjectId: String): Future[Done] = Future {
    blocking {
      keyStore.deleteEntry(dataSubjectId)
      storeAndLoadKeyStore()
      Done
    }
  }

  private def storeAndLoadKeyStore(): Unit = {
    closing(new FileOutputStream(keyStoreFile)) { keyStore.store(_, keyStorePassword) }
    closing(new FileInputStream(keyStoreFile)) { keyStore.load(_, keyStorePassword) }
  }

  private def closing[T <: AutoCloseable, V](closable: T)(block: T => V): V = {
    try {
      block(closable)
    } finally {
      closable.close()
    }
  }
}
