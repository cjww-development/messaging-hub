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
import database.AuditEventStore
import models.auditing.Event
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.Future

trait MockAuditEventStore extends BeforeAndAfterEach with MockitoSugar {
  self: PlaySpec =>

  val mockAuditEventStore: AuditEventStore = mock[AuditEventStore]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditEventStore)
  }

  def mockInsertAuditEvent(inserted: Boolean): OngoingStubbing[Future[MongoCreateResponse]] = {
    when(mockAuditEventStore.insertAuditEvent(any())(any()))
      .thenReturn(if(inserted) Future.successful(MongoSuccessCreate) else Future.successful(MongoFailedCreate))
  }

  def mockRetrieveAuditEvents(events: List[Event]): OngoingStubbing[Future[List[Event]]] = {
    when(mockAuditEventStore.retrieveAuditEvents(any(), any())(any()))
      .thenReturn(Future.successful(events))
  }
}
