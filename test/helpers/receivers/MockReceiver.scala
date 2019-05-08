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

package helpers.receivers

import com.rabbitmq.client.GetResponse
import enums.Acks
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers.any
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import receivers.Receiver

trait MockReceiver extends BeforeAndAfterEach with MockitoSugar {
  self: PlaySpec =>

  val mockReceiver: Receiver = mock[Receiver]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReceiver)
  }

  def mockGetMessageCount(count: Long): OngoingStubbing[Long] = {
    when(mockReceiver.getMessageCount)
      .thenReturn(count)
  }

  def mockSendSuccessfulAck(): OngoingStubbing[Acks.Ack] = {
    when(mockReceiver.sendSuccessfulAck(any()))
      .thenReturn(Acks.Success)
  }

  def mockSendFailedAck(): OngoingStubbing[Acks.Ack] = {
    when(mockReceiver.sendFailedAck(any()))
      .thenReturn(Acks.Success)
  }

  def mockGetMessage(message: GetResponse): OngoingStubbing[GetResponse] = {
    when(mockReceiver.getMessage[GetResponse])
      .thenReturn(message)
  }
}
