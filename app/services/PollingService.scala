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

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.featuremanagement.services.FeatureService
import com.cjwwdev.logging.Logging
import com.cjwwdev.implicits.ImplicitJsValues._
import com.cjwwdev.mongo.responses.{MongoFailedCreate, MongoSuccessCreate}
import com.rabbitmq.client.GetResponse
import database.{AuditEventStore, FeedEventStore}
import models.common.MessageTypes._
import global.FeatureSet
import javax.inject.Inject
import models.feed.{Event => FeedEvent}
import models.auditing.{Event => AuditEvent}
import models.receipts.Receipt
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}
import receivers.Receiver
import utils.Implicits._

import scala.concurrent.{Future, ExecutionContext => ExC}
import scala.util.Try

class PollingService @Inject()(val featureService: FeatureService,
                               val config: ConfigurationLoader,
                               val feedEventStore: FeedEventStore,
                               val auditEventStore: AuditEventStore,
                               val receiver: Receiver,
                               implicit val ec: ExC) extends AutomatedService[Receipt] with Logging {

  override val prefetchCount: Int = config.get[Int]("rabbitMQ.receiving.DefaultReceiver.prefetch")

  override def isEnabled: Boolean = featureService.getState(FeatureSet.feedReceipt).state

  override def invoke(): Future[Receipt] = {
    processFeedMessages map { receipt =>
      logger.info(s"[processFeedMessages] - Number of fetched messages: ${receipt.fetchedMessages}")
      logger.info(s"[processFeedMessages] - Number of accepted messages: ${receipt.acceptedMessages}")
      logger.info(s"[processFeedMessages] - Number of rejected messages: ${receipt.rejectedMessages}")
      logger.info(s"[processFeedMessages] - Job attempted?: ${receipt.attempted}")
      receipt
    }
  }

  def processFeedMessages: Future[Receipt] = {
    receiver.getMessageCount match {
      case 0 =>
        logger.warn(s"[processFeedMessages] - Empty receive; polling will continue")
        Future.successful(Receipt.emptyReceive)
      case x if x < prefetchCount  => processPrefetchMessages(x.toInt)
      case _ => processPrefetchMessages(prefetchCount)
    }
  }

  def processPrefetchMessages(count: Int): Future[Receipt] = {
    Future.sequence(count.toRange.map(_ => validateAndStore(receiver.getMessage)))
      .map(_.toReceipt)
  }

  def validateAndStore(resp: GetResponse): Future[Receipt] = {
    Try(Json.parse(new String(resp.getBody)).as[JsValue]).fold(
      _ => Future.successful(sendFailure(resp, "validateAndStore")),
      json => json.getOption[MessageType]("messageType")
        .map(messageMatcher(resp, json))
        .getOrElse(Future.successful(sendFailure(resp, "validateAndStore")))
    )
  }

  def validateMessage[T](resp: GetResponse, json: JsValue, rds: Reads[T])(f: T => Future[Receipt]): Future[Receipt] = {
    Json.fromJson[T](json)(rds).fold(
      _     => Future.successful(sendFailure(resp, "validateMessage")),
      event => f(event)
    )
  }

  def sendSuccess(resp: GetResponse, location: String): Receipt = {
    logger.info(s"[$location] - Message successfully validated for ${resp.getProps.getCorrelationId}")
    receiver.sendSuccessfulAck(resp.getEnvelope.getDeliveryTag)
    Receipt(1, 1, 0)
  }

  def sendFailure(resp: GetResponse, location: String): Receipt = {
    logger.warn(s"[$location] - Could not validate message for ${resp.getProps.getCorrelationId}")
    receiver.sendFailedAck(resp.getEnvelope.getDeliveryTag)
    Receipt(1, 0, 1)
  }

  override def defaultDisabled: Future[Receipt] = {
    logger.warn("[defaultDisabled] - Feed receipt service disabled; aborting execution")
    Future.successful(Receipt.disabledRun)
  }

  def messageMatcher(resp: GetResponse, json: JsValue): PartialFunction[MessageType, Future[Receipt]] = {
    case AUDIT_EVENT => validateMessage[AuditEvent](resp, json, AuditEvent.receiverFormat) {
      auditEventStore.insertAuditEvent(_).map {
        case MongoSuccessCreate => sendSuccess(resp, "validateAndStore AUDIT_EVENT")
        case MongoFailedCreate  => sendFailure(resp, "validateAndStore AUDIT_EVENT")
      }
    }
    case FEED_EVENT  => validateMessage[FeedEvent](resp, json, FeedEvent.receiverFormat) {
      feedEventStore.insertFeedEvent(_).map{
        case MongoSuccessCreate => sendSuccess(resp, "validateAndStore AUDIT_EVENT")
        case MongoFailedCreate  => sendFailure(resp, "validateAndStore AUDIT_EVENT")
      }
    }
  }
}
