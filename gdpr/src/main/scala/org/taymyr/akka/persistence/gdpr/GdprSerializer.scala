package org.taymyr.akka.persistence.gdpr

import akka.actor.ExtendedActorSystem
import akka.event.Logging
import akka.serialization.{AsyncSerializerWithStringManifest, SerializationExtension, Serializers}
import com.google.protobuf.ByteString
import org.taymyr.akka.persistence.gdpr.GdprSerializer.{SerializerId, WithDataSubjectIdManifest}
import org.taymyr.akka.persistence.gdpr.WithDataSubjectId.shredded
import org.taymyr.akka.persistence.gdpr.scaladsl.GdprEncryption
import org.taymyr.akka.persistence.gdpr.serialization.GDPR

import java.io.NotSerializableException
import scala.concurrent.Future.{failed, successful}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Failure

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/GdprSerializer.html GdprSerializer]]
 */
final class GdprSerializer(system: ExtendedActorSystem) extends AsyncSerializerWithStringManifest(system) {
  private lazy val gdprEncryption = GdprEncryption.get(system)
  private implicit val executor: ExecutionContextExecutor = system.dispatcher
  private val log = Logging.getLogger(system, getClass)

  override def identifier: Int = SerializerId

  override def manifest(obj: AnyRef): String = obj match {
    case _: WithDataSubjectId[_] => WithDataSubjectIdManifest
    case _ =>
      throw new IllegalArgumentException(s"Can't serialize object of type ${obj.getClass.getName} in [${getClass.getName}]")
  }

  override def toBinaryAsync(obj: AnyRef): Future[Array[Byte]] = obj match {
    case withDataSubjectId: WithDataSubjectId[_] => successful(withDataSubjectId)
      .flatMap { withDataSubjectId =>
        toBinaryAsync(withDataSubjectId)
      }
    case _ =>
      failed(new IllegalArgumentException(s"Can't serialize object of type ${obj.getClass.getName} in [${getClass.getName}]"))
  }

  private def toBinaryAsync(obj: WithDataSubjectId[_ <: AnyRef]): Future[Array[Byte]] = successful(obj)
    .flatMap { withDataSubjectId =>
      val payload = withDataSubjectId.payload.orNull[AnyRef]
      val serializer = serialization.findSerializerFor(payload)
      val serializerId = serializer.identifier
      val manifest = Serializers.manifestFor(serializer, payload)
      val unencryptedBytes = serialization.serialize(payload).get

      gdprEncryption.encrypt(unencryptedBytes, withDataSubjectId.dataSubjectId)
        .map { encryptedBytes =>
          GDPR.WithDataSubjectId.newBuilder
            .setDataSubjectId(withDataSubjectId.dataSubjectId)
            .setPayload(ByteString.copyFrom(encryptedBytes))
            .setSerializerId(serializerId)
            .setManifest(manifest)
            .build
            .toByteArray
        }
    }
    .transform {
      case Failure(exception) =>
        val msg = s"Serialization failed in [${getClass.getName}]"
        log.error(exception, msg)
        Failure(new NotSerializableException(msg))
      case success => success
    }

  override def fromBinaryAsync(bytes: Array[Byte], manifest: String): Future[AnyRef] = manifest match {
    case WithDataSubjectIdManifest => Future { GDPR.WithDataSubjectId.parseFrom(bytes) }
        .flatMap { proto =>
          val encryptedBytes = proto.getPayload.toByteArray
          val dataSubjectId = proto.getDataSubjectId
          gdprEncryption.decrypt(encryptedBytes, dataSubjectId) map {
            case Some(unencryptedBytes) =>
              val payload = serialization.deserialize(unencryptedBytes, proto.getSerializerId, proto.getManifest).get
              WithDataSubjectId(dataSubjectId, payload)
            case None => shredded(dataSubjectId)
          }
        }
        .transform {
          case Failure(exception) =>
            val msg = s"Deserialization failed for message with manifest [$manifest] in [${getClass.getName}]"
            log.error(exception, msg)
            Failure(new NotSerializableException(msg))
          case success => success
        }
    case _ =>
      failed(new NotSerializableException(
        s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]"))
  }

  private def serialization = SerializationExtension.get(system)
}

/**
 * Custom implementation of the [[https://doc.akka.io/japi/akka-enhancements/1.1.16/akka/persistence/gdpr/GdprSerializer$.html contract]]
 */
object GdprSerializer {
  val WithDataSubjectIdManifest = "WithDataSubjectId"
  val SerializerId = 892563174
}
