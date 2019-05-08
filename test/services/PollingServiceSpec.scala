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

import java.time.LocalDateTime
import java.util.UUID

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{Envelope, GetResponse}
import helpers.services.ServiceSpec
import models.receipts.Receipt

import scala.concurrent.ExecutionContext.Implicits.global

class PollingServiceSpec extends ServiceSpec {

  mockGetConfig(value = 1)

  val testService = new PollingService(
    mockFeatureService,
    mockConfigLoader,
    mockFeedEventStore,
    mockAuditEventStore,
    mockReceiver,
    global
  )

  val uuid = UUID.randomUUID().toString
  val now  = LocalDateTime.now()

  val props: BasicProperties = new BasicProperties(
    "text/json",
    "plain",
    null,
    2,
    null,
    s"correlationId-$uuid",
    null,
    null,
    s"message-$uuid",
    null,
    null,
    null,
    null,
    null
  )

  val envelope = new Envelope(
    616L,
    true,
    "",
    ""
  )

  val testBody = """{ "abc" : "xyz" }"""

  def testGetResponse(body: Array[Byte]): GetResponse = new GetResponse(
    envelope,
    props,
    body,
    1
  )

  "pollingService run" should {
    "return a success receipt" when {
      "a Feed event has been saved" in {
        val testFeedEvent =
          s"""
            |{
            |   "correlationId" : "correlationId-$uuid",
            |   "messageType" : "FEED_EVENT",
            |   "service" : "testService",
            |   "createdAt" : "$now",
            |   "sessionId" : "session-$uuid",
            |   "userId" : "user-$uuid",
            |   "detail" : {
            |       "feedId" : "feedId-$uuid",
            |       "location" : "testLocation",
            |       "title" : "testTitle",
            |       "description" : "testDesc"
            |   }
            |}
          """.stripMargin

        mockGetState(enabled = true)

        mockGetMessageCount(count = 1)

        mockGetMessage(message = testGetResponse(testFeedEvent.getBytes))

        mockInsertFeedEvent(inserted = true)

        awaitAndAssert(testService.run) {
          _ mustBe Receipt(1, 1, 0)
        }
      }

      "an Audit event has been saved" in {
        val testAuditEvent =
          s"""
             |{
             |   "correlationId" : "correlationId-$uuid",
             |   "messageType" : "AUDIT_EVENT",
             |   "service" : "testService",
             |   "appId" : "testAppId",
             |   "createdAt" : "$now",
             |   "sessionId" : "session-$uuid",
             |   "userId" : "user-$uuid",
             |   "requestId" : "request-$uuid",
             |   "deviceId" : "device-$uuid",
             |   "ipAddress" : "1.1.1.1",
             |   "eventCode" : 1,
             |   "detail" : {
             |      "abc" : "xyz"
             |   }
             |}
          """.stripMargin

        mockGetState(enabled = true)

        mockGetMessageCount(count = 1)

        mockGetMessage(message = testGetResponse(testAuditEvent.getBytes))

        mockInsertAuditEvent(inserted = true)

        awaitAndAssert(testService.run) {
          _ mustBe Receipt(1, 1, 0)
        }
      }
    }

    "return a rejected receipt" when {
      "the message body can't be read into either structure" in {
        mockGetState(enabled = true)

        mockGetMessageCount(count = 1)

        mockGetMessage(message = testGetResponse(testBody.getBytes))

        awaitAndAssert(testService.run) {
          _ mustBe Receipt(1, 0, 1)
        }
      }

      "the message body can't be read into either structure and is not valid json" in {
        mockGetState(enabled = true)

        mockGetMessageCount(count = 1)

        mockGetMessage(message = testGetResponse(Array.emptyByteArray))

        awaitAndAssert(testService.run) {
          _ mustBe Receipt(1, 0, 1)
        }
      }

      "there was a problem inserting a feed event into mongo" in {
        val testFeedEvent =
          s"""
             |{
             |   "correlationId" : "correlationId-$uuid",
             |   "messageType" : "FEED_EVENT",
             |   "service" : "testService",
             |   "createdAt" : "$now",
             |   "sessionId" : "session-$uuid",
             |   "userId" : "user-$uuid",
             |   "detail" : {
             |       "feedId" : "feedId-$uuid",
             |       "location" : "testLocation",
             |       "title" : "testTitle",
             |       "description" : "testDesc"
             |   }
             |}
          """.stripMargin

        mockGetState(enabled = true)

        mockGetMessageCount(count = 1)

        mockGetMessage(message = testGetResponse(testFeedEvent.getBytes))

        mockInsertFeedEvent(inserted = false)

        awaitAndAssert(testService.run) {
          _ mustBe Receipt(1, 0, 1)
        }
      }

      "there was a problem inserting an audit event into mongo" in {
        val testAuditEvent =
          s"""
             |{
             |   "correlationId" : "correlationId-$uuid",
             |   "messageType" : "AUDIT_EVENT",
             |   "service" : "testService",
             |   "appId" : "testAppId",
             |   "createdAt" : "$now",
             |   "sessionId" : "session-$uuid",
             |   "userId" : "user-$uuid",
             |   "requestId" : "request-$uuid",
             |   "deviceId" : "device-$uuid",
             |   "ipAddress" : "1.1.1.1",
             |   "eventCode" : 1,
             |   "detail" : {
             |      "abc" : "xyz"
             |   }
             |}
          """.stripMargin

        mockGetState(enabled = true)

        mockGetMessageCount(count = 1)

        mockGetMessage(message = testGetResponse(testAuditEvent.getBytes))

        mockInsertAuditEvent(inserted = false)

        awaitAndAssert(testService.run) {
          _ mustBe Receipt(1, 0, 1)
        }
      }

      "the message json had a valid message type but didn't follow the types structure" in {
        val testAuditEvent =
          s"""
             |{
             |   "messageType" : "AUDIT_EVENT",
             |   "service" : "testService",
             |   "deviceId" : "device-$uuid",
             |   "ipAddress" : "1.1.1.1",
             |   "eventCode" : 1,
             |   "detail" : {
             |      "abc" : "xyz"
             |   }
             |}
          """.stripMargin

        mockGetState(enabled = true)

        mockGetMessageCount(count = 1)

        mockGetMessage(message = testGetResponse(testAuditEvent.getBytes))

        awaitAndAssert(testService.run) {
          _ mustBe Receipt(1, 0, 1)
        }
      }
    }

    "return an empty receive receipt" when {
      "there are no messages to process" in {
        mockGetState(enabled = true)

        mockGetMessageCount(count = 0)

        awaitAndAssert(testService.run) {
          _ mustBe Receipt.emptyReceive
        }
      }
    }

    "return a disabledRun receipt" when {
      "polling is disabled" in {
        mockGetState(enabled = false)

        awaitAndAssert(testService.run) {
          _ mustBe Receipt.disabledRun
        }
      }
    }
  }
}
