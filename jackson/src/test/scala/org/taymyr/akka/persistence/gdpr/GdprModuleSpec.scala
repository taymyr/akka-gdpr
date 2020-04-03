package org.taymyr.akka.persistence.gdpr

import akka.actor.{ActorSystem, ExtendedActorSystem}
import akka.serialization.Serialization
import akka.testkit.TestKit
import akka.testkit.TestKit.shutdownActorSystem
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.taymyr.akka.persistence.gdpr.jackson.GdprModule
import org.scalatest.OptionValues._
import org.taymyr.akka.persistence.gdpr.model.{IdentityCard, User}

import java.util.UUID

class GdprModuleSpec extends TestKit(
  ActorSystem(
    "GdprModuleSpec",
    ConfigFactory.parseResources("application-test.conf")
      .withFallback(ConfigFactory.load())
      .resolve
  )
)
  with AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  private val objectMapper = new ObjectMapper()
    .registerModule(new GdprModule())
    .registerModule(new ParameterNamesModule())

  override def afterAll(): Unit = {
    shutdownActorSystem(system)
  }

  def withCurrentTransportInformation(block: => Unit): Unit = {
    Serialization.withTransportInformation[Unit](system.asInstanceOf[ExtendedActorSystem])(() => block)
  }

  s"${classOf[GdprModule]}" should {
    "serialize/deserialize objects wrapped with WithDataSubjectId" in withCurrentTransportInformation {
      val login = s"login-${UUID.randomUUID().toString}"
      val identityCard = new IdentityCard(
        "John",
        "Doe",
        "1234",
        "ABC-0345"
      )
      val user = new User(
        login,
        WithDataSubjectId.create(login, identityCard)
      )
      val json = objectMapper.writerFor(classOf[User]).writeValueAsString(user)

      val reader = objectMapper.readerFor(classOf[User])

      // Check JSON structure
      val rootNode = reader.readTree(json)
      rootNode should not be null
      rootNode.getNodeType shouldEqual JsonNodeType.OBJECT

      val identityCardNode = rootNode.get("identityCard")
      identityCardNode should not be null
      identityCardNode.getNodeType shouldEqual JsonNodeType.OBJECT

      val dataSubjectIdNode = identityCardNode.get("dataSubjectId")
      dataSubjectIdNode should not be null
      dataSubjectIdNode.getNodeType shouldEqual JsonNodeType.STRING

      val payloadNode = identityCardNode.get("payload")
      payloadNode should not be null
      payloadNode.getNodeType shouldEqual JsonNodeType.STRING

      // Check JSON deserialization with decryption
      val decryptedUser = reader.readValue[User](json)
      decryptedUser should not be null
      decryptedUser shouldEqual user
      decryptedUser.getIdentityCard should not be null
      decryptedUser.getIdentityCard.payload shouldBe defined
      decryptedUser.getIdentityCard.payload.value shouldEqual identityCard
    }
  }
}
