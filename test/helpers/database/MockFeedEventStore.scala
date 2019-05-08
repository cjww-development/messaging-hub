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

package helpers.database

import com.cjwwdev.mongo.responses.{MongoCreateResponse, MongoFailedCreate, MongoSuccessCreate}
import database.FeedEventStore
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Future

trait MockFeedEventStore extends BeforeAndAfterEach with MockitoSugar {
  self: PlaySpec =>

  val mockFeedEventStore: FeedEventStore = mock[FeedEventStore]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFeedEventStore)
  }

  def mockInsertFeedEvent(inserted: Boolean): OngoingStubbing[Future[MongoCreateResponse]] = {
    when(mockFeedEventStore.insertFeedEvent(any())(any()))
      .thenReturn(if(inserted) Future.successful(MongoSuccessCreate) else Future.successful(MongoFailedCreate))
  }
}
