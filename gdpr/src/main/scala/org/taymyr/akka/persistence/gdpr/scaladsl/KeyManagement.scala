package org.taymyr.akka.persistence.gdpr.scaladsl

import akka.Done
import javax.crypto.SecretKey

import scala.concurrent.Future

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/#akka.persistence.gdpr.scaladsl.KeyManagement KeyManagement]]
 */
trait KeyManagement {
  def getKey(dataSubjectId: String): Future[Option[SecretKey]]

  def getOrCreateKey(dataSubjectId: String): Future[SecretKey]

  def shred(dataSubjectId: String): Future[Done]
}
