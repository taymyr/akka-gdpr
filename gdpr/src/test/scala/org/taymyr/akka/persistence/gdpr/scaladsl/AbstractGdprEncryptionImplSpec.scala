package org.taymyr.akka.persistence.gdpr.scaladsl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestKit.shutdownActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.OptionValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AbstractGdprEncryptionImplSpec
  extends TestKit(
    ActorSystem(
      "GdprEncryptionSpec",
      ConfigFactory.parseResources("scaladsl-application.conf")
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

  def withGdprEncryption(block: AbstractGdprEncryption => Any) {
    val gdprEncryption = Option(GdprEncryption.get(system))
    gdprEncryption shouldBe defined
    assert(gdprEncryption.value.isInstanceOf[AbstractGdprEncryption])
    block(gdprEncryption.value.asInstanceOf[AbstractGdprEncryption])
  }

  s"Scala DSL ${classOf[AbstractGdprEncryption].getName} implementation" should {
    "provide GDPR settings from config" in withGdprEncryption { gdprEncryptionImpl =>
      val gdprSettings = Option(gdprEncryptionImpl.gdprSettings)
      gdprSettings.value.keySize shouldEqual 128
      gdprSettings.value.gcmTlen shouldEqual 96
    }

    "encrypt/decrypt/shred" in withGdprEncryption { gdprEncryption =>
      val payload = "Some Payload A"
      val dataSubjectId = UUID.randomUUID().toString

      val encryptedBytes = Await.result(gdprEncryption.encrypt(payload.getBytes, dataSubjectId), Duration.Inf)
      encryptedBytes should not equal payload.getBytes

      val maybeDecryptedBytes = Await.result(gdprEncryption.decrypt(encryptedBytes, dataSubjectId), Duration.Inf)
      maybeDecryptedBytes shouldBe defined
      payload.getBytes shouldEqual maybeDecryptedBytes.value

      Await.result(gdprEncryption.shred(dataSubjectId), Duration.Inf)
      val maybeShreddedBytes = Await.result(gdprEncryption.decrypt(encryptedBytes, dataSubjectId), Duration.Inf)
      maybeShreddedBytes shouldBe empty
    }
  }
}
