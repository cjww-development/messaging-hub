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

import java.time.{LocalDateTime, ZoneId, ZoneOffset}
import java.util.UUID

import helpers.AssertionHelpers
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

class EventSpec extends PlaySpec with AssertionHelpers {

  val uuid: String = UUID.randomUUID().toString
  val now: LocalDateTime = LocalDateTime.now()

  "An Audit event" should {
    "read from a receiver format and written into a mongo format" in {
      val receivedJson = Json.parse(
        s"""
          |{
          |   "correlationId" : "correlationId-$uuid",
          |   "messageType" : "AUDIT_EVENT",
          |   "service" : "test-service",
          |   "appId" : "testAppId",
          |   "createdAt" : "${now.toString}",
          |   "sessionId" : "session-$uuid",
          |   "userId" : "userId-$uuid",
          |   "requestId" : "requestId-$uuid",
          |   "deviceId" : "deviceId-$uuid",
          |   "ipAddress" : "0.0.0.0",
          |   "eventCode" : 1,
          |   "detail" : {
          |     "abc" : "xyz"
          |   }
          |}
        """.stripMargin
      )

      val outputJson = Json.parse(
        s"""
           |{
           |   "correlationId" : "correlationId-$uuid",
           |   "messageType" : "AUDIT_EVENT",
           |   "service" : "test-service",
           |   "appId" : "testAppId",
           |   "createdAt" : {
           |      "$$date" : ${now.atZone(ZoneId.systemDefault).toInstant.toEpochMilli}
           |   },
           |   "sessionId" : "session-$uuid",
           |   "userId" : "userId-$uuid",
           |   "requestId" : "requestId-$uuid",
           |   "deviceId" : "deviceId-$uuid",
           |   "ipAddress" : "0.0.0.0",
           |   "eventCode" : 1,
           |   "detail" : {
           |     "abc" : "xyz"
           |   }
           |}
        """.stripMargin
      )

      val model = Json.fromJson(receivedJson)(Event.receiverFormat).get

      Json.toJson(model)(Event.receiverFormat) mustBe outputJson
    }

    "read from a mongo format and written into an outbound format" in {
      val outputJson = Json.parse(
        s"""
           |{
           |   "correlationId" : "correlationId-$uuid",
           |   "messageType" : "AUDIT_EVENT",
           |   "service" : "test-service",
           |   "appId" : "testAppId",
           |   "createdAt" : "${now.toString}",
           |   "sessionId" : "session-$uuid",
           |   "userId" : "userId-$uuid",
           |   "requestId" : "requestId-$uuid",
           |   "deviceId" : "deviceId-$uuid",
           |   "ipAddress" : "0.0.0.0",
           |   "eventCode" : 1,
           |   "detail" : {
           |     "abc" : "xyz"
           |   }
           |}
        """.stripMargin
      )

      val receivedJson = Json.parse(
        s"""
           |{
           |   "correlationId" : "correlationId-$uuid",
           |   "messageType" : "AUDIT_EVENT",
           |   "service" : "test-service",
           |   "appId" : "testAppId",
           |   "createdAt" : {
           |      "$$date" : ${now.atZone(ZoneId.systemDefault).toInstant.toEpochMilli}
           |   },
           |   "sessionId" : "session-$uuid",
           |   "userId" : "userId-$uuid",
           |   "requestId" : "requestId-$uuid",
           |   "deviceId" : "deviceId-$uuid",
           |   "ipAddress" : "0.0.0.0",
           |   "eventCode" : 1,
           |   "detail" : {
           |     "abc" : "xyz"
           |   }
           |}
        """.stripMargin
      )

      val model = Json.fromJson(receivedJson)(Event.outboundFormat).get

      Json.toJson(model)(Event.outboundFormat) mustBe outputJson
    }
  }
}
