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

import java.util.Collections

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.logging.Logging
import com.cjwwdev.security.deobfuscation.DeObfuscation.stringDeObfuscate
import com.cjwwdev.security.deobfuscation.DeObfuscator
import com.rabbitmq.client.{Channel, ConnectionFactory, GetResponse}
import enums.Acks.{Ack, Success => AckSuccess}
import enums.Initialisation.{Initialised, Success => InitSuccess}
import javax.inject.Inject

import scala.util.Try

class DefaultReceiver @Inject()(val config: ConfigurationLoader) extends Receiver with Logging {

  private val configPath: String = s"rabbitMQ.receiving.${getClass.getSimpleName}"

  private def getEncodedValue(key: String)(implicit deObfuscator: DeObfuscator[String]): String = {
    deObfuscator.decrypt(config.get[String](s"$configPath.$key")).swap.getOrElse("")
  }

  override val queueName: String          = config.get[String](s"$configPath.queue")
  override val exchangeName: String       = config.get[String](s"$configPath.exchange")
  override val exchangeType: String       = config.get[String](s"$configPath.exchangeType")
  override val routingKey: Option[String] = None

  override val host: String               = config.get[String](s"$configPath.host")
  override val userName: String           = getEncodedValue("user")
  override val password: String           = getEncodedValue("password")

  protected val connectionFactory = new ConnectionFactory()

  connectionFactory.setHost(host)
  connectionFactory.setUsername(userName)
  connectionFactory.setPassword(password)

  override val channel: Channel = connectionFactory
    .newConnection()
    .createChannel()

  setupReceiver()
}

trait Receiver {

  val queueName: String
  val exchangeName: String
  val exchangeType: String
  val routingKey: Option[String]

  val host: String

  val userName: String
  val password: String

  val channel: Channel

  def setupReceiver(): Initialised = {
    Try {
      channel.exchangeDeclare(exchangeName, exchangeType, true)
      channel.queueDeclare(queueName, false, false, false, Collections.emptyMap[String, Object])
      channel.queueBind(queueName, exchangeName, routingKey.getOrElse(""))
    }
    InitSuccess
  }

  def getMessage[A]: GetResponse = {
    channel.basicGet(queueName, false)
  }

  def sendSuccessfulAck(deliveryTag: Long): Ack = {
    channel.basicAck(deliveryTag, false)
    AckSuccess
  }

  def sendFailedAck(deliveryTag: Long): Ack = {
    channel.basicNack(deliveryTag, false, false)
    AckSuccess
  }

  def getMessageCount: Long = {
    channel.messageCount(queueName)
  }
}