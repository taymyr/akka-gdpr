package org.taymyr.akka.persistence.gdpr

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestKit.shutdownActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.OptionValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.taymyr.akka.persistence.gdpr.scaladsl.{AbstractGdprEncryption, GdprEncryption}

import java.io.{File, FileInputStream, FileOutputStream}
import java.security.KeyStore
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class JavaKeyStoreGdprEncryptionSpec
  extends TestKit(
  ActorSystem(
    "JavaKeyStoreGdprEncryptionSpec",
    ConfigFactory.parseResources("application-test.conf")
      .withFallback(ConfigFactory.load())
      .resolve
  )
)
  with AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  private val jcsSettings = JCASettings(system, "akka.persistence.gdpr.jca-provider")
  private val keyStoreFile = new File(jcsSettings.location)
  private val keyStore = KeyStore.getInstance(jcsSettings.keystoreType)

  override def beforeAll(): Unit = {
    if (keyStoreFile.exists()) {
      keyStoreFile.delete()
    }
    keyStoreFile.createNewFile()
    // create a new KeyStore
    closing(new FileOutputStream(keyStoreFile)) { fos =>
      keyStore.load(null, jcsSettings.password.toCharArray)  // initialize
      keyStore.store(fos, jcsSettings.password.toCharArray) // save
    }
  }

  override def afterAll(): Unit = {
    if (keyStoreFile.exists()) {
      keyStoreFile.delete()
    }
    shutdownActorSystem(system)
  }

  def withGdprEncryption(block: AbstractGdprEncryption => Any) {
    val gdprEncryption = Option(GdprEncryption.get(system))
    gdprEncryption shouldBe defined
    assert(gdprEncryption.value.isInstanceOf[AbstractGdprEncryption])
    block(gdprEncryption.value.asInstanceOf[AbstractGdprEncryption])
  }

  s"${classOf[JavaKeyStoreGdprEncryption]}" should {
    "encryp/decrypt/shred" in withGdprEncryption { gdprEncryption =>
      val payload = "Some Payload A"
      val dataSubjectId = UUID.randomUUID().toString

      val encryptedBytes = Await.result(gdprEncryption.encrypt(payload.getBytes, dataSubjectId), Duration.Inf)
      encryptedBytes should not equal payload.getBytes

      reload(keyStore)
      keyStore.getKey(dataSubjectId, jcsSettings.password.toCharArray) should not be null

      val maybeDecryptedBytes = Await.result(gdprEncryption.decrypt(encryptedBytes, dataSubjectId), Duration.Inf)
      maybeDecryptedBytes shouldBe defined
      payload.getBytes shouldEqual maybeDecryptedBytes.value

      Await.result(gdprEncryption.shred(dataSubjectId), Duration.Inf)
      val maybeShreddedBytes = Await.result(gdprEncryption.decrypt(encryptedBytes, dataSubjectId), Duration.Inf)
      maybeShreddedBytes shouldBe empty

      reload(keyStore)
      keyStore.containsAlias(dataSubjectId) shouldBe false
    }
  }

  private def closing[T <: AutoCloseable, V](closeable: T)(block: T => V): Unit = {
    try {
      block(closeable)
    } finally {
      closeable.close()
    }
  }

  private def reload(ks: KeyStore): Unit = closing(new FileInputStream(keyStoreFile)) { ks.load(_, jcsSettings.password.toCharArray) }
}
