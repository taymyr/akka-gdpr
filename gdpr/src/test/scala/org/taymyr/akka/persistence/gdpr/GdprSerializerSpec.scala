package org.taymyr.akka.persistence.gdpr

import akka.actor.ActorSystem
import akka.serialization.SerializationExtension
import akka.testkit.TestKit
import akka.testkit.TestKit.shutdownActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration._

class GdprSerializerSpec
  extends TestKit(
    ActorSystem(
      "GdprEncryptionSpec",
      ConfigFactory.parseResources("gdpr-serializer-test.conf")
        .withFallback(ConfigFactory.load())
        .resolve
    )
  )
  with AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override def afterAll(): Unit = {
    shutdownActorSystem(system)
  }

  s"${classOf[GdprSerializer].getName}" should {
    "serialize/deserialize" in {
      val withDataSubjectId = WithDataSubjectId(dataSubjectId = UUID.randomUUID().toString, payload = UUID.randomUUID().toString)
      val serializer = SerializationExtension.get(system).findSerializerFor(withDataSubjectId)
      serializer should not be null
      assert(serializer.isInstanceOf[GdprSerializer])

      val gdprSerializer = serializer.asInstanceOf[GdprSerializer]
      val manifest = gdprSerializer.manifest(withDataSubjectId)
      val encryptedBytes = Await.result(gdprSerializer.toBinaryAsync(withDataSubjectId), 5 seconds)
      encryptedBytes should not be null
      Await.result(gdprSerializer.fromBinaryAsync(encryptedBytes, manifest), 5 seconds) shouldEqual withDataSubjectId
    }
  }
}
