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

import scala.concurrent.{Future, ExecutionContext => ExC}

trait AutomatedService[A] {
  def isEnabled: Boolean

  def invoke(): Future[A]

  def defaultDisabled: Future[A]

  val prefetchCount: Int

  def run(implicit ec: ExC): Future[A] = {
    runGuard(
      enabled  = invoke(),
      disabled = defaultDisabled
    )
  }

  private def runGuard(enabled: => Future[A], disabled: => Future[A]): Future[A] = {
    if(isEnabled) enabled else disabled
  }
}
