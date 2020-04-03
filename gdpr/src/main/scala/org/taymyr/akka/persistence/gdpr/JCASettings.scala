package org.taymyr.akka.persistence.gdpr

import akka.actor.ActorSystem
import com.typesafe.config.Config

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/JcaSettings.html JcaSettings]]
 */
final class JCASettings(val keystoreType: String, val password: String, val location: String, val dispatcher: String)

/**
 * Custom implementation of the [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/JcaSettings$.html JcaSettings]]
 */
object JCASettings {
  val ValidKeyStores = Set("jceks", "pkcs12")

  def apply(config: Config): JCASettings = new JCASettings(
    keystoreType = config.getString("keystore-type"),
    password = config.getString("keystore-password"),
    location = config.getString("keystore-location"),
    dispatcher = config.getString("use-dispatcher")
  )

  def apply(system: ActorSystem, configPath: String): JCASettings = apply(system.settings.config.getConfig(configPath))

  /**
   * Java API.
   */
  def create(config: Config): JCASettings = apply(config)

  /**
   * Java API.
   */
  def create(system: ActorSystem, configPath: String): JCASettings = apply(system, configPath)
}
