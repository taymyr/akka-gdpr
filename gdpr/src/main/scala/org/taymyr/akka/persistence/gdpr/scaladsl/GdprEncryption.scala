package org.taymyr.akka.persistence.gdpr.scaladsl

import akka.Done
import akka.actor.{ActorSystem, ClassicActorSystemProvider, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import org.taymyr.akka.persistence.gdpr.javadsl.{GdprEncryption => JavaGdprEncryption}

import scala.concurrent.Future

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/#akka.persistence.gdpr.scaladsl.GdprEncryption GdprEncryption]]
 */
trait GdprEncryption extends Extension {
  def decrypt(payload: Array[Byte], dataSubjectId: String): Future[Option[Array[Byte]]]

  def encrypt(payload: Array[Byte], dataSubjectId: String): Future[Array[Byte]]

  def shred(dataSubjectId: String): Future[Done]
}

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/#akka.persistence.gdpr.scaladsl.GdprEncryption$ GdprEncryption$]]
 */
object GdprEncryption extends ExtensionId[GdprEncryption] with ExtensionIdProvider {

  val gdprEncrypterConfigPath = "akka.persistence.gdpr.encryption-provider"

  override def createExtension(system: ExtendedActorSystem): GdprEncryption = {
    val config = system.settings.config
    val encryptionProviderConfigPath = config.getString(gdprEncrypterConfigPath)
    val encryptionProviderClassName = config.getString(encryptionProviderConfigPath + ".class")
    val encryptionProviderClass = Class.forName(encryptionProviderClassName, false, system.dynamicAccess.classLoader)

    if (classOf[GdprEncryption].isAssignableFrom(encryptionProviderClass))
      encryptionProviderClass.getDeclaredConstructor(classOf[ExtendedActorSystem], classOf[String])
        .newInstance(system, encryptionProviderConfigPath)
        .asInstanceOf[GdprEncryption]
    else if (classOf[JavaGdprEncryption].isAssignableFrom(encryptionProviderClass))
      new DelegatingGdprEncryption(
        encryptionProviderClass
          .getDeclaredConstructor(classOf[ExtendedActorSystem], classOf[String])
          .newInstance(system, encryptionProviderConfigPath)
          .asInstanceOf[JavaGdprEncryption]
      )
    else
      throw new ClassCastException(
        s"Class ${encryptionProviderClass.getName} should extend " +
          s"${classOf[GdprEncryption].getName} or ${classOf[JavaGdprEncryption].getName}"
      )
  }

  override def lookup(): ExtensionId[_ <: Extension] = GdprEncryption

  /**
   * Java API.
   */
  override def get(system: ActorSystem): GdprEncryption = super.get(system)

  /**
   * Java API.
   */
  override def get(system: ClassicActorSystemProvider): GdprEncryption = super.get(system)
}
