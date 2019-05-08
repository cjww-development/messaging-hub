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
import models.common.MessageTypes
import models.feed.{Detail, Event}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

class FeedEventStoreISpec extends IntegrationSpec with TestDataGenerator {

  val feedEventStore: FeedEventStore = app.injector.instanceOf[FeedEventStore]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(feedEventStore.collection.flatMap(_.remove(BSONDocument())))
  }

  "insertFeedEvent" should {
    "successfully insert the document" in {
      val feedEvent = Event(
        correlationId = generateTestSystemId("correlationId"),
        messageType   = MessageTypes.FEED_EVENT,
        service       = "test-service",
        createdAt     = now,
        sessionId     = generateTestSystemId(SESSION),
        userId        = generateTestSystemId(USER),
        detail        = Detail(
          feedId      = generateTestSystemId(FEED_ITEM),
          location    = "/test/url",
          description = "Test desc",
          title       = "Test title"
        )
      )

      awaitAndAssert(feedEventStore.insertFeedEvent(feedEvent)) {
        _ mustBe MongoSuccessCreate
      }
    }

    "return a MongoFailedCreate" when {
      "a document already exists with an identifier that's the same" in {
        val feedEvent = Event(
          correlationId = generateTestSystemId("correlationId"),
          messageType   = MessageTypes.FEED_EVENT,
          service       = "test-service",
          createdAt     = now,
          sessionId     = generateTestSystemId(SESSION),
          userId        = generateTestSystemId(USER),
          detail        = Detail(
            feedId      = generateTestSystemId(FEED_ITEM),
            location    = "/test/url",
            description = "Test desc",
            title       = "Test title"
          )
        )

        await(feedEventStore.insertFeedEvent(feedEvent))

        awaitAndAssert(feedEventStore.insertFeedEvent(feedEvent)) {
          _ mustBe MongoFailedCreate
        }
      }
    }
  }

  "retrieveFeedEvent" should {
    "return a feed event document" in {
      val feedEvent = Event(
        correlationId = generateTestSystemId("correlationId"),
        messageType   = MessageTypes.FEED_EVENT,
        service       = "test-service",
        createdAt     = now,
        sessionId     = generateTestSystemId(SESSION),
        userId        = generateTestSystemId(USER),
        detail        = Detail(
          feedId      = generateTestSystemId(FEED_ITEM),
          location    = "/test/url",
          description = "Test desc",
          title       = "Test title"
        )
      )

      await(feedEventStore.insertFeedEvent(feedEvent))

      awaitAndAssert(feedEventStore.retrieveFeedEvent(BSONDocument("correlationId" -> feedEvent.correlationId))) {
        _ mustBe Some(feedEvent)
      }
    }

    "return no feed event" in {
      awaitAndAssert(feedEventStore.retrieveFeedEvent(BSONDocument("correlationId" -> generateTestSystemId("correlationId")))) {
        _ mustBe None
      }
    }
  }

  "retrieveFeedEvents" should {
    "return a populated list of feed events" in {
      val feedEvent = Event(
        correlationId = generateTestSystemId("correlationId"),
        messageType   = MessageTypes.FEED_EVENT,
        service       = "test-service",
        createdAt     = now,
        sessionId     = generateTestSystemId(SESSION),
        userId        = generateTestSystemId(USER),
        detail        = Detail(
          feedId      = generateTestSystemId(FEED_ITEM),
          location    = "/test/url",
          description = "Test desc",
          title       = "Test title"
        )
      )

      await(feedEventStore.insertFeedEvent(feedEvent))

      awaitAndAssert(feedEventStore.retrieveFeedEvents(BSONDocument("correlationId" -> feedEvent.correlationId))) {
        _ mustBe List(feedEvent)
      }
    }

    "return an empty list" in {
      awaitAndAssert(feedEventStore.retrieveFeedEvents(BSONDocument("correlationId" -> generateTestSystemId("correlationId")))) {
        _ mustBe List.empty[Event]
      }
    }
  }
}