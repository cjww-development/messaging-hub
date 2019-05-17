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

package models.auditing

import java.time.LocalDateTime

import com.cjwwdev.json.TimeFormat
import models.common.MessageTypes.MessageType
import models.EventFormatting
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Event(correlationId: String,
                 messageType: MessageType,
                 service: String,
                 appId: String,
                 createdAt: LocalDateTime,
                 sessionId: String,
                 userId: String,
                 requestId: String,
                 deviceId: String,
                 ipAddress: String,
                 eventCode: Int,
                 detail: JsValue)

object Event extends EventFormatting[Event] with EnvReads with EnvWrites with TimeFormat {

  implicit val receiverFormat: OFormat[Event] = (
    (__ \ "correlationId").format[String] and
    (__ \ "messageType").format[MessageType] and
    (__ \ "service").format[String] and
    (__ \ "appId").format[String] and
    (__ \ "createdAt").format[LocalDateTime](DefaultLocalDateTimeReads)(dateTimeWriteLDT) and
    (__ \ "sessionId").format[String] and
    (__ \ "userId").format[String] and
    (__ \ "requestId").format[String] and
    (__ \ "deviceId").format[String] and
    (__ \ "ipAddress").format[String] and
    (__ \ "eventCode").format[Int] and
    (__ \ "detail").format[JsValue]
  )(Event.apply, unlift(Event.unapply))

  implicit val outboundFormat: OFormat[Event] = (
    (__ \ "correlationId").format[String] and
    (__ \ "messageType").format[MessageType] and
    (__ \ "service").format[String] and
    (__ \ "appId").format[String] and
    (__ \ "createdAt").format[LocalDateTime](DefaultLocalDateTimeWrites)(dateTimeReadLDT) and
    (__ \ "sessionId").format[String] and
    (__ \ "userId").format[String] and
    (__ \ "requestId").format[String] and
    (__ \ "deviceId").format[String] and
    (__ \ "ipAddress").format[String] and
    (__ \ "eventCode").format[Int] and
    (__ \ "detail").format[JsValue]
  )(Event.apply, unlift(Event.unapply))

  implicit val outboundSeq: Writes[Seq[Event]] = new Writes[Seq[Event]] {
    override def writes(o: Seq[Event]): JsValue = {
      o.foldRight(Json.arr()) { (event, json) =>
        json ++ Json.arr(Json.toJson(event)(outboundFormat))
      }
    }
  }
}
