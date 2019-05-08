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

package models.common

import play.api.libs.json._

object MessageTypes {
  sealed trait MessageType

  case object AUDIT_EVENT extends MessageType
  case object FEED_EVENT  extends MessageType

  implicit val format: Format[MessageType] = new Format[MessageType] {
    override def writes(msgType: MessageType): JsValue = {
      JsString(msgType.toString)
    }

    override def reads(json: JsValue): JsResult[MessageType] = {
      json.as[String] match {
        case "AUDIT_EVENT" => JsSuccess(AUDIT_EVENT)
        case "FEED_EVENT"  => JsSuccess(FEED_EVENT)
      }
    }
  }
}
