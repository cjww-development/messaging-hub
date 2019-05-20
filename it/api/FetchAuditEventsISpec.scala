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

package api

import java.time.LocalDate

import com.cjwwdev.testing.integration.application.IntegrationApplication
import database.AuditEventStore
import helpers.IntegrationSpec
import models.auditing.Event
import models.common.MessageTypes
import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.WSRequest
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.Future

class FetchAuditEventsISpec extends IntegrationSpec with IntegrationApplication {
  override val currentAppBaseUrl: String = "messages"
  override val appConfig: Map[String, Any] = Map(
    "database.DefaultAuditEventStore.database"   -> "test-messaging-hub-db",
    "database.DefaultAuditEventStore.collection" -> "test-audit-event-store",
    "database.DefaultFeedEventStore.database"    -> "test-messaging-hub-db",
    "database.DefaultFeedEventStore.collection"  -> "test-feed-event-store",
    "play.modules.disabled"                      -> List("global.MessagePollingBindings")
  )

  val repo = app.injector.instanceOf[AuditEventStore]

  def client(url: String): WSRequest = ws.url(url)

  val testEventOne = Event(
    correlationId = "testCorrelationId1",
    messageType   =  MessageTypes.AUDIT_EVENT,
    service       = "testService",
    appId         = "testAppId",
    createdAt     = LocalDate.of(2019,1,1).atStartOfDay(),
    sessionId     = "testSessionId1",
    userId        = "testUserId",
    requestId     = "testRequestId1",
    deviceId      = "testDeviceId",
    ipAddress     = "testIpAddr",
    eventCode     = 1,
    detail        = Json.parse("""{ "abc" : "xyz" }""")
  )

  val testEventTwo = Event(
    correlationId = "testCorrelationId2",
    messageType   =  MessageTypes.AUDIT_EVENT,
    service       = "testService",
    appId         = "testAppId",
    createdAt     = LocalDate.of(2019,1,2).atStartOfDay(),
    sessionId     = "testSessionId2",
    userId        = "testUserId",
    requestId     = "testRequestId2",
    deviceId      = "testDeviceId",
    ipAddress     = "testIpAddr",
    eventCode     = 1,
    detail        = Json.parse("""{ "abc" : "xyz" }""")
  )

  val testEventThree = Event(
    correlationId = "testCorrelationId3",
    messageType   =  MessageTypes.AUDIT_EVENT,
    service       = "testService",
    appId         = "testAppId",
    createdAt     = LocalDate.of(2019,1,3).atStartOfDay(),
    sessionId     = "testSessionId3",
    userId        = "testUserId",
    requestId     = "testRequestId3",
    deviceId      = "testDeviceId",
    ipAddress     = "testIpAddr",
    eventCode     = 101,
    detail        = Json.parse("""{ "abc" : "xyz" }""")
  )

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    await(Future.sequence(List(testEventOne, testEventTwo, testEventThree).map(repo.insertAuditEvent)))
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    await(repo.collection.flatMap(_.remove(BSONDocument())))
  }

  "/messaging/auditing/events?userId=testUserId" should {
    "return all saved audit events" when {
      "only the userId parameter is provided" in {
        awaitAndAssert(client(s"$testAppUrl/auditing/events?userId=testUserId").get()) { resp =>
          resp.status mustBe OK
          Json.parse(resp.body).\("body").get mustBe Json.toJson(List(testEventThree, testEventTwo, testEventOne))
        }
      }
    }
  }

  "/messaging/auditing/events?userId=testUserId&start=2019-01-01&end=2019-01-02" should {
    "return two saved audit events" when {
      "a start and end date are provided" in {
        awaitAndAssert(client(s"$testAppUrl/auditing/events?userId=testUserId&start=2019-01-01&end=2019-01-02").get()) { resp =>
          resp.status mustBe OK
          Json.parse(resp.body).\("body").get mustBe Json.toJson(List(testEventTwo, testEventOne))
        }
      }
    }
  }

  "/messaging/auditing/events?userId=testUserId&start=2019-01-02" should {
    "return two saved audit events" when {
      "a start date is provided" in {
        awaitAndAssert(client(s"$testAppUrl/auditing/events?userId=testUserId&start=2019-01-02").get()) { resp =>
          resp.status mustBe OK
          Json.parse(resp.body).\("body").get mustBe Json.toJson(List(testEventThree, testEventTwo))
        }
      }
    }
  }

  "/messaging/auditing/events?userId=testUserId&start=2019-01-02&types=101" should {
    "return one saved audit event" when {
      "a start date is provided and specific types are defined" in {
        awaitAndAssert(client(s"$testAppUrl/auditing/events?userId=testUserId&start=2019-01-02&types=101").get()) { resp =>
          resp.status mustBe OK
          Json.parse(resp.body).\("body").get mustBe Json.toJson(List(testEventThree))
        }
      }
    }
  }

  "/messaging/auditing/events" should {
    "return a Bad request" when {
      "no userId is provided" in {
        awaitAndAssert(client(s"$testAppUrl/auditing/events").get()) { resp =>
          resp.status mustBe BAD_REQUEST
          Json.parse(resp.body).\("errorMessage").get mustBe JsString("No userId provided")
        }
      }
    }
  }
}
