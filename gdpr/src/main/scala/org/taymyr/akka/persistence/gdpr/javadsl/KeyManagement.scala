package org.taymyr.akka.persistence.gdpr.javadsl

import java.util.Optional
import java.util.concurrent.CompletionStage

import akka.Done
import javax.crypto.SecretKey

/**
 * Custom implementation of [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/javadsl/KeyManagement.html KeyManagement]]
 */
trait KeyManagement {
  def getKey(dataSubjectId: String): CompletionStage[Optional[SecretKey]]

  def getOrCreateKey(dataSubjectId: String): CompletionStage[SecretKey]

  def shred(dataSubjectId: String): CompletionStage[Done]
}
