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

package receivers

import java.util.UUID

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{Channel, Envelope, GetResponse}
import enums.Initialisation.{Success => InitSuccess}
import enums.Acks.{Success => AckSuccess}
import helpers.AssertionHelpers
import helpers.rabbit.MockChannel
import org.scalatestplus.play.PlaySpec

class ReceiverSpec extends PlaySpec with MockChannel with AssertionHelpers {

  val testReceiver: Receiver = new Receiver {
    override val queueName: String          = "testQueue"
    override val exchangeName: String       = "testExchange"
    override val exchangeType: String       = "testExchangeType"
    override val routingKey: Option[String] = None
    override val host: String               = "testHost"
    override val userName: String           = "testUser"
    override val password: String           = "testPass"
    override val channel: Channel           = mockChannel
  }

  "setupReceiver" should {
    "return Initialised" in {
      mockExchangeDeclare()
      mockQueueDeclare()
      mockQueueBlind()

      assertResults(testReceiver.setupReceiver()) {
        _ mustBe InitSuccess
      }
    }
  }

  "getMessage" should {
    "return a GetResponse" in {
      val uuid = UUID.randomUUID().toString

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

      val testGetResponse = new GetResponse(
        envelope,
        props,
        Array.emptyByteArray,
        1
      )

      mockBasicGet(resp = testGetResponse)

      assertResults(testReceiver.getMessage[GetResponse]) {
        _ mustBe testGetResponse
      }
    }
  }

  "sendSuccessfulAck" should {
    "return a SuccessAck" in {
      mockBasicAck()

      assertResults(testReceiver.sendSuccessfulAck(1L)) {
        _ mustBe AckSuccess
      }
    }
  }

  "sendFailedAck" should {
    "return a SuccessAck" in {
      mockBasicNack()

      assertResults(testReceiver.sendFailedAck(1L)) {
        _ mustBe AckSuccess
      }
    }
  }

  "getMessageCount" should {
    "return an Int" in {
      mockGetMessageCount(count = 616L)

      assertResults(testReceiver.getMessageCount) {
        _ mustBe 616L
      }
    }
  }
}
