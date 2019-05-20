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

import com.cjwwdev.mongo.responses.{MongoFailedCreate, MongoSuccessCreate}
import helpers.{IntegrationSpec, TestDataGenerator}
import models.auditing.Event
import models.common.MessageTypes
import play.api.Configuration
import play.api.libs.json.Json
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.Future

class AuditEventStoreISpec extends IntegrationSpec with TestDataGenerator {

  val auditEventStore: AuditEventStore = new DefaultAuditEventStore(Configuration(
    "database.DefaultAuditEventStore.uri"        -> "mongodb://localhost:27017",
    "database.DefaultAuditEventStore.database"   -> "test-messaging-hub-db",
    "database.DefaultAuditEventStore.collection" -> "test-audit-event-store"
  ))

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(auditEventStore.collection.flatMap(_.remove(BSONDocument())))
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    await(Future.sequence(auditEventStore.indexes map auditEventStore.ensureSingleIndex))
  }

  "insertAuditEvent" should {
    "successfully insert the document" in {
      val auditEvent = Event(
        correlationId = generateTestSystemId("correlationId"),
        messageType   = MessageTypes.AUDIT_EVENT,
        service       = "test-service",
        appId         = "testAppId",
        createdAt     = now,
        sessionId     = generateTestSystemId(SESSION),
        userId        = generateTestSystemId(USER),
        requestId     = generateTestSystemId(REQUEST),
        deviceId      = generateTestSystemId(DEVICE),
        ipAddress     = "0.0.0.0",
        eventCode     = 1,
        detail        = Json.parse("""{ "a" : "b" }""")
      )

      awaitAndAssert(auditEventStore.insertAuditEvent(auditEvent)) {
        _ mustBe MongoSuccessCreate
      }
    }

    "return a MongoFailedCreate" when {
      "a document already exists with an identifier that's the same" in {
        val auditEvent = Event(
          correlationId = generateTestSystemId("correlationId"),
          messageType   = MessageTypes.AUDIT_EVENT,
          service       = "test-service",
          appId         = "testAppId",
          createdAt     = now,
          sessionId     = generateTestSystemId(SESSION),
          userId        = generateTestSystemId(USER),
          requestId     = generateTestSystemId(REQUEST),
          deviceId      = generateTestSystemId(DEVICE),
          ipAddress     = "0.0.0.0",
          eventCode     = 1,
          detail        = Json.parse("""{ "a" : "b" }""")
        )

        await(auditEventStore.insertAuditEvent(auditEvent))

        awaitAndAssert(auditEventStore.insertAuditEvent(auditEvent)) {
          _ mustBe MongoFailedCreate
        }
      }
    }
  }

  "retrieveAuditEvent" should {
    "return a feed event document" in {
      val auditEvent = Event(
        correlationId = generateTestSystemId("correlationId"),
        messageType   = MessageTypes.AUDIT_EVENT,
        service       = "test-service",
        appId         = "testAppId",
        createdAt     = now,
        sessionId     = generateTestSystemId(SESSION),
        userId        = generateTestSystemId(USER),
        requestId     = generateTestSystemId(REQUEST),
        deviceId      = generateTestSystemId(DEVICE),
        ipAddress     = "0.0.0.0",
        eventCode     = 1,
        detail        = Json.parse("""{ "a" : "b" }""")
      )

      await(auditEventStore.insertAuditEvent(auditEvent))

      awaitAndAssert(auditEventStore.retrieveAuditEvent(Json.obj("correlationId" -> auditEvent.correlationId))) {
        _ mustBe Some(auditEvent)
      }
    }

    "return no feed event" in {
      awaitAndAssert(auditEventStore.retrieveAuditEvent(Json.obj("correlationId" -> generateTestSystemId("correlationId")))) {
        _ mustBe None
      }
    }
  }

  "retrieveAuditEvents" should {
    "return a populated list of feed events" in {
      val auditEvent = Event(
        correlationId = generateTestSystemId("correlationId"),
        messageType   = MessageTypes.AUDIT_EVENT,
        service       = "test-service",
        appId         = "testAppId",
        createdAt     = now,
        sessionId     = generateTestSystemId(SESSION),
        userId        = generateTestSystemId(USER),
        requestId     = generateTestSystemId(REQUEST),
        deviceId      = generateTestSystemId(DEVICE),
        ipAddress     = "0.0.0.0",
        eventCode     = 1,
        detail        = Json.parse("""{ "a" : "b" }""")
      )

      await(auditEventStore.insertAuditEvent(auditEvent))

      awaitAndAssert(auditEventStore.retrieveAuditEvents(Json.obj("correlationId" -> auditEvent.correlationId))) {
        _ mustBe List(auditEvent)
      }
    }

    "return an empty list" in {
      val cid = generateTestSystemId("correlationId")

      awaitAndAssert(auditEventStore.retrieveAuditEvents(Json.obj("correlationId" -> cid))) {
        _ mustBe List.empty[Event]
      }
    }
  }
}
