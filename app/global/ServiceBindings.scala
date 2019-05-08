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

package global

import com.cjwwdev.config.{ConfigurationLoader, DefaultConfigurationLoader}
import com.cjwwdev.featuremanagement.models.Features
import com.cjwwdev.logging.filters.{DefaultRequestLoggingFilter, RequestLoggingFilter}
import database.{AuditEventStore, DefaultAuditEventStore, DefaultFeedEventStore, FeedEventStore}
import jobs.MessagePollingJob
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import receivers.{DefaultReceiver, Receiver}
import services.PollingService

class ServiceBindings extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    bindGlobals() ++ bindRabbit() ++ bindJobs() ++ bindServices() ++ bindStores()
  }

  def bindGlobals(): Seq[Binding[_]] = Seq(
    bind(classOf[ConfigurationLoader]).to(classOf[DefaultConfigurationLoader]).eagerly(),
    bind(classOf[Features]).to(classOf[FeatureDef]).eagerly(),
    bind(classOf[RequestLoggingFilter]).to(classOf[DefaultRequestLoggingFilter]).eagerly(),
    bind(classOf[MessagingHubIndexing]).toSelf.eagerly()
  )

  def bindRabbit(): Seq[Binding[_]] = Seq(
    bind(classOf[Receiver]).to(classOf[DefaultReceiver]).eagerly()
  )

  def bindJobs(): Seq[Binding[_]] = Seq(
    bind(classOf[MessagePollingJob]).toSelf.eagerly()
  )

  def bindServices(): Seq[Binding[_]] = Seq(
    bind(classOf[PollingService]).toSelf.eagerly()
  )

  def bindStores(): Seq[Binding[_]] = Seq(
    bind(classOf[FeedEventStore]).to(classOf[DefaultFeedEventStore]).eagerly(),
    bind(classOf[AuditEventStore]).to(classOf[DefaultAuditEventStore]).eagerly()
  )
}