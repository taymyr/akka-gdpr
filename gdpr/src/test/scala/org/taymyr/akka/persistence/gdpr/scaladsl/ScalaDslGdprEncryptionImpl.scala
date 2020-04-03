package org.taymyr.akka.persistence.gdpr.scaladsl

import akka.actor.ExtendedActorSystem

class ScalaDslGdprEncryptionImpl(system: ExtendedActorSystem, config: String) extends AbstractGdprEncryption(system) {
  private val keyManagementImpl = new ScalaDslKeyManagementImpl(system)
  override protected def keyManagement(): KeyManagement = keyManagementImpl
}
