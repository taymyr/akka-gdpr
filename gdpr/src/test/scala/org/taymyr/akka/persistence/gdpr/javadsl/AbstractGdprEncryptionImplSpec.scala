package org.taymyr.akka.persistence.gdpr.javadsl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestKit.shutdownActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.OptionValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.util.UUID
import java.util.concurrent.CompletionStage

class AbstractGdprEncryptionImplSpec
  extends TestKit(
    ActorSystem(
      "GdprEncryptionSpec",
      ConfigFactory.parseResources("javadsl-application.conf")
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

  s"Java DSL ${classOf[AbstractGdprEncryption].getName} implementation" should {
    "encrypt/decrypt/shred" in withGdprEncryption { gdprEncryption =>
      val payload = "Some Payload A"
      val dataSubjectId = UUID.randomUUID().toString

      val encryptedBytes = await(gdprEncryption.encrypt(payload.getBytes, dataSubjectId))
      encryptedBytes should not equal payload.getBytes

      val maybeDecryptedBytes = await(gdprEncryption.decrypt(encryptedBytes, dataSubjectId))
      assert(maybeDecryptedBytes.isPresent)
      maybeDecryptedBytes.get shouldEqual payload.getBytes

      await(gdprEncryption.shred(dataSubjectId))
      val maybeShreddedBytes = await(gdprEncryption.decrypt(encryptedBytes, dataSubjectId))
      assert(!maybeShreddedBytes.isPresent)
    }
  }

  private def await[T](cs: CompletionStage[T]): T = cs.toCompletableFuture.join()
}
