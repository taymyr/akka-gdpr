package org.taymyr.akka.persistence.gdpr

import java.io.Serializable
import java.util.Optional

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/WithDataSubjectId.html WithDataSubjectId]]
 */
final case class WithDataSubjectId[T <: AnyRef](dataSubjectId: String, payload: Option[T]) extends Serializable {
  import scala.compat.java8.OptionConverters._

  /**
   * Java API
   */
  def getPayload: Optional[_ <: T] = payload.asJava

  def isShredded: Boolean = Option(payload).isEmpty
}

/**
 * Custom implementation of the [[https://doc.akka.io/api/akka-enhancements/1.1.16/akka/persistence/gdpr/WithDataSubjectId$.html WithDataSubjectId]]
 */
object WithDataSubjectId {

  def apply[T <: AnyRef](dataSubjectId: String, payload: T): WithDataSubjectId[T] = apply(dataSubjectId, Option[T](payload))

  def shredded[T <: AnyRef](dataSubjectId: String): WithDataSubjectId[T] = new WithDataSubjectId[T](dataSubjectId, None)

  /**
   * Java API
   */
  def create[T <: AnyRef](dataSubjectId: String, payload: T): WithDataSubjectId[T] = WithDataSubjectId[T](dataSubjectId, payload)

  /**
   * Java API
   */
  def withDataSubjectId[T <: AnyRef](dataSubjectId: String, payload: T): WithDataSubjectId[T] = create[T](dataSubjectId, payload)

  /**
   * Java API
   */
  def shredded[T <: AnyRef](payloadClass: Class[T], dataSubjectId: String): WithDataSubjectId[T] = shredded[T](dataSubjectId)
}
