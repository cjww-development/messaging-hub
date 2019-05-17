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

import java.time.{LocalDate, ZoneOffset}

import database.AuditEventStore
import javax.inject.Inject
import models.auditing.Event
import org.joda.time.DateTime
import play.api.libs.json.{JsNumber, JsObject, Json}
import play.api.mvc.Request

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultFetchService @Inject()(val auditEventStore: AuditEventStore) extends FetchService

trait FetchService {

  val auditEventStore: AuditEventStore

  def getEvents(userId: String)(implicit req: Request[_], ec: ExC): Future[List[Event]] = {
    val limit = req.getQueryString("limit").map(_.toInt).getOrElse(-1)
    auditEventStore.retrieveAuditEvents(collateQuery, limit)
  }

  def collateQuery(implicit req: Request[_]): JsObject = {
    userIdSegment ++ dateRangeSegment ++ serviceSegment ++ sessionIdSegment
      .++(requestIdSegment) ++ deviceIdSegment ++ ipAddressSegment ++ eventCodeSegment
  }

  private def userIdSegment(implicit req: Request[_]): JsObject = Json.obj(
    "userId" -> req.getQueryString("userId").get
  )

  private def dateRangeSegment(implicit req: Request[_]): JsObject = {
    val startDate = req.getQueryString("start").map(DateTime.parse)
    val endDate   = req.getQueryString("end").map(DateTime.parse)

    startDate -> endDate match {
      case (Some(sd), Some(ed)) => Json.obj(
        "createdAt" -> Json.obj(
          "$gte" -> Json.obj(
            "$date" -> JsNumber(sd.getMillis)
          ),
          "$lte" -> Json.obj(
            "$date" -> JsNumber(ed.getMillis)
          )
        )
      )
      case (Some(sd), None) => Json.obj(
        "createdAt" -> Json.obj(
          "$gte" -> Json.obj(
            "$date" -> JsNumber(sd.getMillis)
          )
        )
      )
      case (None, _) => Json.obj()
    }
  }

  private def serviceSegment(implicit req: Request[_]): JsObject = {
    req.getQueryString("service").fold(Json.obj())(srv => Json.obj("service" -> srv))
  }

  private def sessionIdSegment(implicit req: Request[_]): JsObject = {
    req.getQueryString("sessionId").fold(Json.obj())(sId => Json.obj("sessionId" -> sId))
  }

  private def requestIdSegment(implicit req: Request[_]): JsObject = {
    req.getQueryString("requestId").fold(Json.obj())(rId => Json.obj("requestId" -> rId))
  }

  private def deviceIdSegment(implicit req: Request[_]): JsObject = {
    req.getQueryString("deviceId").fold(Json.obj())(dId => Json.obj("deviceId" -> dId))
  }

  private def ipAddressSegment(implicit req: Request[_]): JsObject = {
    req.getQueryString("ipAddress").fold(Json.obj())(ip => Json.obj("ipAddress" -> ip))
  }

  private def eventCodeSegment(implicit req: Request[_]): JsObject = {
    req.getQueryString("types").fold(Json.obj()) { codes =>
      Json.obj(
        "$or" -> codes.split("-").foldLeft(Json.arr()) { (arr, code) =>
          arr ++ Json.arr(Json.obj("eventCode" -> code.toInt))
        }
      )
    }
  }
}
