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
import models.feed.Event
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Request
import reactivemongo.api.Cursor
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.core.errors.DatabaseException
import reactivemongo.play.json._

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultFeedEventStore @Inject()(val config: Configuration) extends FeedEventStore with ConnectionSettings

trait FeedEventStore extends DatabaseRepository with Logging with Logger {

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
    )
  )

  def insertFeedEvent(feedEvent: Event)(implicit ec: ExC): Future[MongoCreateResponse] = {
    collection.flatMap {
      _.insert(feedEvent)(Event.receiverFormat, implicitly) map { _ =>
        logger.info(s"[insertFeedEvent] - Created Event of type ${feedEvent.messageType} against correlationId ${feedEvent.correlationId}")
        MongoSuccessCreate
      } recover {
        case e: DatabaseException =>
          e.code match {
            case Some(11000) =>
              logger.warn(s"[insertFeedEvent] - Attempted insert of document ${feedEvent.correlationId} of type ${feedEvent.messageType}: Duplicated event")
            case _ =>
              logger.error(s"[insertFeedEvent] - There was a problem inserting document ${feedEvent.correlationId} of type ${feedEvent.messageType}")
          }
          MongoFailedCreate
      }
    }
  }

  def retrieveFeedEvent(selector: BSONDocument)(implicit ec: ExC): Future[Option[Event]] = {
    for {
      col <- collection
      fev <- col
        .find[BSONDocument](selector)
        .one[Event](Event.outboundFormat, ec)
    } yield fev
  }

  def retrieveFeedEvents(selector: BSONDocument, maxDocs: Int = -1)(implicit ec: ExC): Future[List[Event]] = {
    for {
      col  <- collection
      list <- col
        .find[BSONDocument](selector)
        .sort(Json.obj("createdAt" -> -1))
        .cursor[Event]()(Event.outboundFormat, implicitly)
        .collect[List](maxDocs, Cursor.FailOnError[List[Event]]())
    } yield list
  }
}
