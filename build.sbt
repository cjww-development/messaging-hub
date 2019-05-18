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

import com.typesafe.config.ConfigFactory
import com.typesafe.sbt.packager.docker.Cmd
import scoverage.ScoverageKeys

import scala.util.{Failure, Success, Try}

val appName = "messaging-hub"

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

lazy val scoverageSettings = Seq(
  ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;config.*;global.*;jobs.*;utils.*;views.*;.*(AuthService|BuildInfo|Routes).*",
  ScoverageKeys.coverageMinimum          := 80,
  ScoverageKeys.coverageFailOnMinimum    := false,
  ScoverageKeys.coverageHighlighting     := true
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala)
  .settings(scoverageSettings)
  .configs(IntegrationTest)
  .settings(PlayKeys.playDefaultPort := 8000)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    name                                          :=  """messaging-hub""",
    version                                       :=  btVersion,
    scalaVersion                                  :=  "2.12.8",
    organization                                  :=  "com.cjww-dev.apps",
    resolvers                                     ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      "cjww-dev"       at "http://dl.bintray.com/cjww-development/releases"
    ),
    libraryDependencies                           ++= AppDependencies(),
    libraryDependencies                           +=  filters,
    bintrayOrganization                           :=  Some("cjww-development"),
    bintrayReleaseOnPublish in ThisBuild          :=  true,
    bintrayRepository                             :=  "releases",
    bintrayOmitLicense                            :=  true,
    Keys.fork in IntegrationTest                  :=  false,
    unmanagedSourceDirectories in IntegrationTest :=  (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    parallelExecution in IntegrationTest          :=  false,
    dockerRepository                              :=  Some("cjwwdevelopment"),
    dockerCommands                                :=  Seq(
      Cmd("FROM", "openjdk:8u181-jdk"),
      Cmd("WORKDIR", "/opt/docker"),
      Cmd("ADD", "--chown=daemon:daemon opt /opt"),
      Cmd("USER", "daemon"),
      Cmd("ENTRYPOINT", """["/opt/docker/bin/messaging-hub"]"""),
      Cmd("CMD", """[]""")
    )
  )
