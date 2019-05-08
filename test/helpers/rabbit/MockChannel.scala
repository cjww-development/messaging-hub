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

package helpers.rabbit

import com.rabbitmq.client.AMQP.Exchange.{DeclareOk => ExchangeDeclareOk}
import com.rabbitmq.client.AMQP.Queue.{BindOk, DeclareOk => QueueDeclareOk}
import com.rabbitmq.client.{Channel, GetResponse}
import com.rabbitmq.client.impl.AMQImpl.{Exchange, Queue}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

trait MockChannel extends BeforeAndAfterEach with MockitoSugar {
  self: PlaySpec =>

  val mockChannel: Channel = mock[Channel]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockChannel)
  }

  def mockExchangeDeclare(): OngoingStubbing[ExchangeDeclareOk] = {
    when(mockChannel.exchangeDeclare(any[String](), any[String](), any[Boolean]()))
      .thenReturn(new Exchange.DeclareOk())
  }

  def mockQueueDeclare(): OngoingStubbing[QueueDeclareOk] = {
    when(mockChannel.queueDeclare(any[String](), any[Boolean](), any[Boolean](), any[Boolean](), any[java.util.Map[String, Object]]()))
      .thenReturn(new Queue.DeclareOk("testQueue", 1, 1))
  }

  def mockQueueBlind(): OngoingStubbing[BindOk] = {
    when(mockChannel.queueBind(any[String](), any[String](), any[String]()))
      .thenReturn(new Queue.BindOk())
  }

  def mockBasicAck(): Unit = {
    doNothing()
      .when(mockChannel)
      .basicAck(any[Long](), any[Boolean]())
  }

  def mockBasicNack(): Unit = {
    doNothing()
      .when(mockChannel)
      .basicNack(any[Long](), any[Boolean](), any[Boolean]())
  }

  def mockGetMessageCount(count: Long): OngoingStubbing[Long] = {
    when(mockChannel.messageCount(any[String]()))
      .thenReturn(count)
  }

  def mockBasicGet(resp: GetResponse): OngoingStubbing[GetResponse] = {
    when(mockChannel.basicGet(any[String](), any[Boolean]()))
      .thenReturn(resp)
  }
}
