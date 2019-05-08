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

import akka.actor.ActorRef
import com.cjwwdev.logging.Logging
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import messages.ScheduledMessage
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait ScheduledJob extends Logging {
  val message: ScheduledMessage[_]

  val applicationLifecycle: ApplicationLifecycle

  val jobName: String
  val expression: String

  val scheduler: QuartzSchedulerExtension
  val receiver: ActorRef

  lazy val setupSchedule: Unit = {
    scheduler.createSchedule(jobName, None, expression)
    scheduler.schedule(jobName, receiver, message)
  }

  applicationLifecycle.addStopHook { () =>
    Future.successful {
      scheduler.cancelJob(jobName)
      scheduler.shutdown(waitForJobsToComplete = false)
    }
  }
}
