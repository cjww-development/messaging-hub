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

package models.feed

import java.time.LocalDateTime

import com.cjwwdev.json.TimeFormat
import models.EventFormatting
import models.common.MessageTypes.MessageType
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Event(correlationId: String,
                 messageType: MessageType,
                 service: String,
                 createdAt: LocalDateTime,
                 sessionId: String,
                 userId: String,
                 detail: Detail)

case class Detail(feedId: String,
                  location: String,
                  title: String,
                  description: String)

object Event extends EventFormatting[Event] with EnvReads with EnvWrites with TimeFormat {

  private val feedDetailFormat: OFormat[Detail] = (
    (__ \ "feedId").format[String] and
    (__ \ "location").format[String] and
    (__ \ "title").format[String] and
    (__ \ "description").format[String]
  )(Detail.apply, unlift(Detail.unapply))

  override implicit val receiverFormat: OFormat[Event] = (
    (__ \ "correlationId").format[String] and
    (__ \ "messageType").format[MessageType] and
    (__ \ "service").format[String] and
    (__ \ "createdAt").format[LocalDateTime](DefaultLocalDateTimeReads)(dateTimeWriteLDT) and
    (__ \ "sessionId").format[String] and
    (__ \ "userId").format[String] and
    (__ \ "detail").format[Detail](feedDetailFormat)
  )(Event.apply, unlift(Event.unapply))

  override implicit val outboundFormat: OFormat[Event] = (
    (__ \ "correlationId").format[String] and
    (__ \ "messageType").format[MessageType] and
    (__ \ "service").format[String] and
    (__ \ "createdAt").format[LocalDateTime](DefaultLocalDateTimeWrites)(dateTimeReadLDT) and
    (__ \ "sessionId").format[String] and
    (__ \ "userId").format[String] and
    (__ \ "detail").format[Detail](feedDetailFormat)
  )(Event.apply, unlift(Event.unapply))
}