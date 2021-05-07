package org.taymyr.akka.persistence.gdpr

import akka.actor.ExtendedActorSystem
import org.taymyr.akka.persistence.gdpr.scaladsl.{AbstractGdprEncryption, KeyManagement}

class TestGdprEncryptionImpl(system: ExtendedActorSystem, config: String) extends AbstractGdprEncryption(system) {
  private val keyManagementImpl = new TestKeyManagementImpl(system)
  override protected def keyManagement(): KeyManagement = keyManagementImpl
}
