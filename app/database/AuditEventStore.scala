/*
 * Copyright 2019 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package database

import com.cjwwdev.logging.Logging
import com.cjwwdev.logging.output.Logger
import com.cjwwdev.mongo.DatabaseRepository
import com.cjwwdev.mongo.connection.ConnectionSettings
import com.cjwwdev.mongo.responses.{MongoCreateResponse, MongoFailedCreate, MongoSuccessCreate}
import javax.inject.Inject
import models.auditing.Event
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.Cursor
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json._

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultAuditEventStore @Inject()(val config: Configuration) extends AuditEventStore with ConnectionSettings

trait AuditEventStore extends DatabaseRepository with Logging with Logger {

  override val indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("correlationId" -> IndexType.Ascending),
      name   = Some("CorrelationId"),
      unique = true,
      sparse = false
    ),
    Index(
      key    = Seq("messageType" -> IndexType.Ascending),
      name   = Some("MessageType"),
      unique = false,
      sparse = false
    ),
    Index(
      key    = Seq("service" -> IndexType.Ascending),
      name   = Some("Service"),
      unique = false,
      sparse = false
    ),
    Index(
      key    = Seq("sessionId" -> IndexType.Ascending),
      name   = Some("SessionId"),
      unique = false,
      sparse = false
    ),
    Index(
      key    = Seq("userId" -> IndexType.Ascending),
      name   = Some("UserId"),
      unique = false,
      sparse = false
    ),
    Index(
      key    = Seq("requestId" -> IndexType.Ascending),
      name   = Some("RequestId"),
      unique = false,
      sparse = false
    ),
    Index(
      key    = Seq("deviceId" -> IndexType.Ascending),
      name   = Some("DeviceId"),
      unique = false,
      sparse = false
    ),
    Index(
      key    = Seq("ipAddress" -> IndexType.Ascending),
      name   = Some("IPAddress"),
      unique = false,
      sparse = false
    ),
    Index(
      key    = Seq("eventCode" -> IndexType.Ascending),
      name   = Some("EventCode"),
      unique = false,
      sparse = false
    )
  )

  def insertAuditEvent(auditEvent: Event)(implicit ec: ExC): Future[MongoCreateResponse] = {
    collection flatMap {
      _.insert(auditEvent)(Event.receiverFormat, ec) map { _ =>
        logger.info(s"[insertAuditEvent] - Created Event of type ${auditEvent.messageType} against correlationId ${auditEvent.correlationId}")
        MongoSuccessCreate
      } recover {
        case e: DatabaseException =>
          e.code match {
            case Some(11000) =>
              logger.warn(s"[insertAuditEvent] - Attempted insert of document ${auditEvent.correlationId} of type ${auditEvent.messageType}: Duplicated event")
            case _ =>
              logger.error(s"[insertAuditEvent] - There was a problem inserting document ${auditEvent.correlationId} of type ${auditEvent.messageType}")
          }
          MongoFailedCreate
      }
    }
  }

  def retrieveAuditEvent(selector: JsObject)(implicit ec: ExC): Future[Option[Event]] = {
    for {
      col <- collection
      fev <- col
        .find[JsObject](selector)
        .one[Event](Event.outboundFormat, ec)
    } yield fev
  }

  def retrieveAuditEvents(selector: JsObject, maxDocs: Int = -1)(implicit ec: ExC): Future[List[Event]] = {
    for {
      col  <- collection
      list <- col
        .find[JsObject](selector)
        .sort(Json.obj("createdAt" -> -1))
        .cursor[Event]()(Event.outboundFormat, implicitly)
        .collect[List](maxDocs, Cursor.FailOnError[List[Event]]())
    } yield list
  }
}
