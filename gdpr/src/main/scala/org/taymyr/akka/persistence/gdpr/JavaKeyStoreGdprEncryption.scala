package org.taymyr.akka.persistence.gdpr

import akka.actor.ExtendedActorSystem
import org.taymyr.akka.persistence.gdpr.scaladsl.{AbstractGdprEncryption, KeyManagement}

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/JavaKeyStoreGdprEncryption.html JavaKeyStoreGdprEncryption]].
 */
class JavaKeyStoreGdprEncryption(system: ExtendedActorSystem, configPath: String) extends AbstractGdprEncryption(system) {
  private val keyManager = new JCAKeyManagement(
    system,
    gdprSettings,
    JCASettings(system.settings.config.getConfig(configPath))
  )
  override protected def keyManagement(): KeyManagement = keyManager
}
