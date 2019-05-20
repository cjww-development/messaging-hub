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

import jobs.MessagePollingJob
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}
import receivers.{DefaultReceiver, Receiver}
import services.PollingService

class MessagePollingBindings extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind(classOf[Receiver]).to(classOf[DefaultReceiver]).eagerly(),
    bind(classOf[MessagePollingJob]).toSelf.eagerly(),
    bind(classOf[PollingService]).toSelf.eagerly()
  )
}
