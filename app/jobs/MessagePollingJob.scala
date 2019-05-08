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

package jobs

import akka.actor.{ActorRef, ActorSystem, Props}
import com.cjwwdev.config.ConfigurationLoader
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import javax.inject.Inject
import messages.{FeedMessage, ScheduledMessage}
import play.api.inject.ApplicationLifecycle
import services.PollingService

class MessagePollingJob @Inject()(val feedReceiptService: PollingService,
                                  val applicationLifecycle: ApplicationLifecycle,
                                  val config: ConfigurationLoader) extends ScheduledJob {

  override val message: ScheduledMessage[_]        = FeedMessage(feedReceiptService)
  override val jobName: String                     = "FeedReceipt"
  override val expression: String                  = config.get[String](s"schedules.$jobName.expression").toString
  private  val system                              = ActorSystem(jobName)
  override val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(system)
  override val receiver: ActorRef                  = system.actorOf(Props(new QuartzActor))

  setupSchedule
}
