package org.taymyr.akka.persistence.gdpr.scaladsl
import java.util.concurrent.ConcurrentHashMap

import akka.Done
import akka.actor.ActorSystem
import javax.crypto.{KeyGenerator, SecretKey}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}

class ScalaDslKeyManagementImpl(system: ActorSystem) extends KeyManagement {
  private implicit val executor: ExecutionContextExecutor = system.dispatcher
  private val keyStore = new ConcurrentHashMap[String, SecretKey].asScala

  override def getKey(dataSubjectId: String): Future[Option[SecretKey]] = Future {
    keyStore.get(dataSubjectId)
  }

  override def getOrCreateKey(dataSubjectId: String): Future[SecretKey] = Future {
    keyStore.get(dataSubjectId) match {
      case None =>
        val key = KeyGenerator.getInstance("AES").generateKey
        keyStore += (dataSubjectId -> key)
        key
      case Some(key) => key
    }
  }

  override def shred(dataSubjectId: String): Future[Done] = Future {
    keyStore -= dataSubjectId
    Done
  }
}
