package org.taymyr.akka.persistence.gdpr

import akka.actor.{ActorSystem, ExtendedActorSystem}
import akka.serialization.Serialization
import akka.testkit.TestKit
import akka.testkit.TestKit.shutdownActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.taymyr.akka.persistence.gdpr.model.{IdentityCard, User}
import org.taymyr.akka.persistence.gdpr.playjson.GdprFormat
import play.api.libs.json.{JsObject, JsString, Json}

import java.util.UUID

class GdprFormatSpec extends TestKit(
  ActorSystem(
    "GdprFormatSpec",
    ConfigFactory.parseResources("application-test.conf")
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

  def withCurrentTransportInformation(block: => Unit): Unit = {
    Serialization.withTransportInformation[Unit](system.asInstanceOf[ExtendedActorSystem])(() => block)
  }

  "GdprFormat" should {
    "serialize/deserialize WithDataSubjectId wrapped fields" in withCurrentTransportInformation {

      val login = s"login-${UUID.randomUUID().toString}"
      val identityCard = new IdentityCard(
        "John",
        "Doe",
        "1234",
        "ABC-0345"
      )
      val user = new User(
        login,
        WithDataSubjectId(login, identityCard)
      )
      val json = Json.toJson(user).toString()

      // Check JSON structure
      val rootJsValue = Json.parse(json)
      assert(rootJsValue.isInstanceOf[JsObject])

      val identityCardJsValue = (rootJsValue \ "identityCard").get
      assert(identityCardJsValue.isInstanceOf[JsObject])

      val dataSubjectIdJsValue = (identityCardJsValue \ "dataSubjectId").get
      assert(dataSubjectIdJsValue.isInstanceOf[JsString])

      val payloadJsValue = (identityCardJsValue \ "payload").get
      assert(payloadJsValue.isInstanceOf[JsString])

      // Check JSON deserialization with decryption
      val maybeDecryptedUser = Json.fromJson[User](rootJsValue).asOpt
      maybeDecryptedUser shouldBe defined
      maybeDecryptedUser shouldEqual Some(user)
      maybeDecryptedUser map(_.identityCard) shouldBe defined
      maybeDecryptedUser map(_.identityCard) flatMap(_.payload) shouldBe defined
      maybeDecryptedUser map(_.identityCard) flatMap(_.payload) shouldEqual Some(identityCard)
    }

    "serialize/deserialize top-level WithDataSubjectId objects" in withCurrentTransportInformation {
      import GdprFormat.withDataSubjectIdFormat // import Format[WithDataSubjectId[_]] in the implicit scope
      val identityCard = new IdentityCard(
        "John",
        "Doe",
        "1234",
        "ABC-0345"
      )
      val withDataSubjectId = WithDataSubjectId(s"login-${UUID.randomUUID().toString}", identityCard)

      val json = Json.toJson[WithDataSubjectId[_]](withDataSubjectId).toString()

      // Check JSON structure
      val rootJsValue = Json.parse(json)
      assert(rootJsValue.isInstanceOf[JsObject])

      val dataSubjectIdJsValue = (rootJsValue \ "dataSubjectId").get
      assert(dataSubjectIdJsValue.isInstanceOf[JsString])

      val payloadJsValue = (rootJsValue \ "payload").get
      assert(payloadJsValue.isInstanceOf[JsString])

      // Check JSON deserialization with decryption
      val maybeDecryptedWithDataSubjectId = Json.fromJson[WithDataSubjectId[_]](rootJsValue).asOpt
      maybeDecryptedWithDataSubjectId shouldBe defined
      maybeDecryptedWithDataSubjectId flatMap(_.payload) shouldEqual Some(identityCard)
    }
  }
}
