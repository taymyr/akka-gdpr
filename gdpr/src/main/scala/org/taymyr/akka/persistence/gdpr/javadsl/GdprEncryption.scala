package org.taymyr.akka.persistence.gdpr.javadsl

import akka.Done
import akka.actor.{ActorSystem, ClassicActorSystemProvider, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import org.taymyr.akka.persistence.gdpr.scaladsl.GdprEncryption.gdprEncrypterConfigPath
import org.taymyr.akka.persistence.gdpr.scaladsl.{GdprEncryption => ScalaGdprEncryption}

import java.util.Optional
import java.util.concurrent.CompletionStage

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/javadsl/GdprEncryption.html GdprEncryption]]
 */
abstract class GdprEncryption extends Extension {
  def decrypt(payload: Array[Byte], dataSubjectId: String): CompletionStage[Optional[Array[Byte]]]

  def encrypt(payload: Array[Byte], dataSubjectId: String): CompletionStage[Array[Byte]]

  def shred(dataSubjectId: String): CompletionStage[Done]
}

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/javadsl/GdprEncryption$.html GdprEncryption]]
 */
object GdprEncryption extends ExtensionId[GdprEncryption] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): GdprEncryption = {
    val config = system.settings.config
    val encryptionProviderConfigPath = config.getString(gdprEncrypterConfigPath)
    val encryptionProviderClassName = config.getString(encryptionProviderConfigPath + ".class")
    val encryptionProviderClass = Class.forName(encryptionProviderClassName, false, system.dynamicAccess.classLoader)

    if (classOf[GdprEncryption].isAssignableFrom(encryptionProviderClass))
      encryptionProviderClass.getDeclaredConstructor(classOf[ExtendedActorSystem], classOf[String])
        .newInstance(system, encryptionProviderConfigPath)
        .asInstanceOf[GdprEncryption]
    else if (classOf[ScalaGdprEncryption].isAssignableFrom(encryptionProviderClass))
      new DelegatingGdprEncryption(
        encryptionProviderClass
          .getDeclaredConstructor(classOf[ExtendedActorSystem], classOf[String])
          .newInstance(system, encryptionProviderConfigPath)
          .asInstanceOf[ScalaGdprEncryption]
      )
    else
      throw new ClassCastException(
        s"Class ${encryptionProviderClass.getClass.getName} should extend " +
          s"${classOf[GdprEncryption].getName} or ${classOf[ScalaGdprEncryption].getName}"
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
