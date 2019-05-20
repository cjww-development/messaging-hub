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

package services

import java.time.{LocalDate, ZoneOffset}

import database.AuditEventStore
import helpers.services.ServiceSpec
import models.auditing.Event
import models.common.MessageTypes
import org.joda.time.DateTime
import play.api.libs.json.{JsNumber, Json}
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.Implicits.global

class FetchServiceSpec extends ServiceSpec {

  val testService = new FetchService {
    override val auditEventStore: AuditEventStore = mockAuditEventStore
  }

  val today = DateTime.parse("2019-01-01").getMillis

  "getEvents" should {
    "return a list of events" when {
      "provided a query" in {
        val req = FakeRequest("GET", "/test/url?userId=testUserId")

        val testEvent = Event(
          correlationId = "testCorrelationId",
          messageType   =  MessageTypes.AUDIT_EVENT,
          service       = "testService",
          appId         = "testAppId",
          createdAt     = LocalDate.of(2019,1,1).atStartOfDay(),
          sessionId     = "testSessionId",
          userId        = "testUserId",
          requestId     = "testRequestId",
          deviceId      = "testDeviceId",
          ipAddress     = "testIpAddr",
          eventCode     = 1,
          detail        = Json.parse("""{ "abc" : "xyz" }""")
        )

        mockRetrieveAuditEvents(events = List(testEvent))

        awaitAndAssert(testService.getEvents("testUserId")(req, implicitly)) {
          _ mustBe List(testEvent)
        }
      }
    }
  }

  "collateQuery" should {
    "construct a full query" when {
      "all query parameters are provided" in {
        val req = FakeRequest("GET", "/test/url" +
          "?userId=testUserId" +
          s"&start=2019-01-01" +
          s"&end=2019-01-01" +
          "&service=testService" +
          "&sessionId=testSessionId" +
          "&requestId=testRequestId" +
          "&deviceId=testDeviceId" +
          "&ipAddress=testIpAddr" +
          "&types=100-101")

        assertResults(testService.collateQuery(req)) {
          _ mustBe Json.obj(
            "userId" -> "testUserId",
            "createdAt" -> Json.obj(
              "$gte" -> Json.obj(
                "$date" -> JsNumber(today)
              ),
              "$lte" -> Json.obj(
                "$date" -> JsNumber(today)
              )
            ),
            "service" -> "testService",
            "sessionId" -> "testSessionId",
            "requestId" -> "testRequestId",
            "deviceId" -> "testDeviceId",
            "ipAddress" -> "testIpAddr",
            "$or" -> Json.arr(
              Json.obj("eventCode" -> 100),
              Json.obj("eventCode" -> 101)
            )
          )
        }
      }
    }

    "construct a partial query" when {
      "only a userId is provided" in {
        val req = FakeRequest("GET", "/test/url" + "?userId=testUserId")

        assertResults(testService.collateQuery(req)) {
          _ mustBe Json.obj(
            "userId" -> "testUserId"
          )
        }
      }

      "only a start date is provided" in {
        val req = FakeRequest("GET", "/test/url" +
          "?userId=testUserId" +
          s"&start=2019-01-01" +
          "&service=testService" +
          "&sessionId=testSessionId" +
          "&requestId=testRequestId" +
          "&deviceId=testDeviceId" +
          "&ipAddress=testIpAddr" +
          "&types=100-101")

        assertResults(testService.collateQuery(req)) {
          _ mustBe Json.obj(
            "userId" -> "testUserId",
            "createdAt" -> Json.obj(
              "$gte" -> Json.obj(
                "$date" -> JsNumber(today)
              )
            ),
            "service" -> "testService",
            "sessionId" -> "testSessionId",
            "requestId" -> "testRequestId",
            "deviceId" -> "testDeviceId",
            "ipAddress" -> "testIpAddr",
            "$or" -> Json.arr(
              Json.obj("eventCode" -> 100),
              Json.obj("eventCode" -> 101)
            )
          )
        }
      }

      "no start or end date is provided" in {
        val req = FakeRequest("GET", "/test/url" +
          "?userId=testUserId" +
          "&service=testService" +
          "&sessionId=testSessionId" +
          "&requestId=testRequestId" +
          "&deviceId=testDeviceId" +
          "&ipAddress=testIpAddr" +
          "&types=100-101")

        assertResults(testService.collateQuery(req)) {
          _ mustBe Json.obj(
            "userId" -> "testUserId",
            "service" -> "testService",
            "sessionId" -> "testSessionId",
            "requestId" -> "testRequestId",
            "deviceId" -> "testDeviceId",
            "ipAddress" -> "testIpAddr",
            "$or" -> Json.arr(
              Json.obj("eventCode" -> 100),
              Json.obj("eventCode" -> 101)
            )
          )
        }
      }

      "no event codes are provided" in {
        val req = FakeRequest("GET", "/test/url" +
          "?userId=testUserId" +
          s"&start=2019-01-01" +
          s"&end=2019-01-01" +
          "&service=testService" +
          "&sessionId=testSessionId" +
          "&requestId=testRequestId" +
          "&deviceId=testDeviceId" +
          "&ipAddress=testIpAddr")

        assertResults(testService.collateQuery(req)) {
          _ mustBe Json.obj(
            "userId" -> "testUserId",
            "createdAt" -> Json.obj(
              "$gte" -> Json.obj(
                "$date" -> JsNumber(today)
              ),
              "$lte" -> Json.obj(
                "$date" -> JsNumber(today)
              )
            ),
            "service" -> "testService",
            "sessionId" -> "testSessionId",
            "requestId" -> "testRequestId",
            "deviceId" -> "testDeviceId",
            "ipAddress" -> "testIpAddr"
          )
        }
      }

      "no sessionId is provided" in {
        val req = FakeRequest("GET", "/test/url" +
          "?userId=testUserId" +
          s"&start=2019-01-01" +
          s"&end=2019-01-01" +
          "&service=testService" +
          "&requestId=testRequestId" +
          "&deviceId=testDeviceId" +
          "&ipAddress=testIpAddr" +
          "&types=100-101")

        assertResults(testService.collateQuery(req)) {
          _ mustBe Json.obj(
            "userId" -> "testUserId",
            "createdAt" -> Json.obj(
              "$gte" -> Json.obj(
                "$date" -> JsNumber(today)
              ),
              "$lte" -> Json.obj(
                "$date" -> JsNumber(today)
              )
            ),
            "service" -> "testService",
            "requestId" -> "testRequestId",
            "deviceId" -> "testDeviceId",
            "ipAddress" -> "testIpAddr",
            "$or" -> Json.arr(
              Json.obj("eventCode" -> 100),
              Json.obj("eventCode" -> 101)
            )
          )
        }
      }
    }

    "throw an None.get exception" when {
      "there is no userId present in the request" in {
        val req = FakeRequest("GET", "/test/url")

        intercept[NoSuchElementException](testService.collateQuery(req))
      }
    }
  }
}
