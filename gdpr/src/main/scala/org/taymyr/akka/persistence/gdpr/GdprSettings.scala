package org.taymyr.akka.persistence.gdpr

import akka.actor.ActorSystem
import com.typesafe.config.Config

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/GdprSettings.html GdprSettings]]
 */
final class GdprSettings(val keySize: Int, val gcmTlen: Int) {
  def withKeySize(keySize: Int): GdprSettings = new GdprSettings(keySize, gcmTlen)

  def withGcmTlen(gcmTlen: Int): GdprSettings = new GdprSettings(keySize, gcmTlen)
}

/**
 * Custom implementation of the [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/GdprSettings$.html GdprSettings$]]
 */
object GdprSettings {

  def apply(config: Config): GdprSettings = new GdprSettings(
    keySize = config.getInt("key-size"),
    gcmTlen = config.getInt("gcm-tlen")
  )

  def apply(system: ActorSystem): GdprSettings = apply(system.settings.config.getConfig("akka.persistence.gdpr"))

  /**
   * Java API
   */
  def create(config: Config): GdprSettings = apply(config)

  /**
   * Java API
   */
  def create(system: ActorSystem): GdprSettings = apply(system)
}
