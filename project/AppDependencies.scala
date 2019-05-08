/*
 * Copyright 2018 CJWW Development
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

import sbt._
import play.sbt.PlayImport._

object AppDependencies {
  def apply(): Seq[ModuleID] = CompileDependencies() ++ UnitTestDependencies() ++ IntegrationTestDependencies()
}

private object CompileDependencies {
  private val appUtilsVersion            = "4.6.1"
  private val serviceHealthVersion       = "0.3.1"
  private val featureManagementVersion   = "1.6.0"
  private val loggingUtilsVersion        = "1.3.1"
  private val ampqClientVersion          = "5.6.0"
  private val akkaQuartzSchedulerVersion = "1.8.0-akka-2.5.x"
  private val reactiveMongoVersion       = "7.3.0"

  private val playImports: Seq[ModuleID] = Seq(filters, guice)

  private val compileDependencies: Seq[ModuleID] = Seq(
    "com.cjww-dev.libs" %  "reactive-mongo_2.12"        % reactiveMongoVersion,
    "com.cjww-dev.libs" %  "application-utilities_2.12" % appUtilsVersion,
    "com.cjww-dev.libs" %  "service-health_2.12"        % serviceHealthVersion,
    "com.cjww-dev.libs" %  "feature-management_2.12"    % featureManagementVersion,
    "com.cjww-dev.libs" %  "logging-utils_2.12"         % loggingUtilsVersion,
    "com.rabbitmq"      %  "amqp-client"                % ampqClientVersion,
    "com.enragedginger" %% "akka-quartz-scheduler"      % akkaQuartzSchedulerVersion
  )

  def apply(): Seq[ModuleID] = compileDependencies ++ playImports
}

private trait TestDependencies {
  val scope: Configuration
  val testDependencies: Seq[ModuleID]
}

private object UnitTestDependencies extends TestDependencies {
  override val scope: Configuration = Test
  override val testDependencies: Seq[ModuleID] = Seq(
    "com.cjww-dev.libs" % "testing-framework_2.12" % "3.2.0" % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}

private object IntegrationTestDependencies extends TestDependencies {
  override val scope: Configuration = IntegrationTest
  override val testDependencies: Seq[ModuleID] = Seq(
    "com.cjww-dev.libs" % "testing-framework_2.12" % "3.2.0" % scope
  )

  def apply(): Seq[ModuleID] = testDependencies
}
