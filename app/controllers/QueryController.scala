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

package controllers

import com.cjwwdev.responses.ApiResponse
import javax.inject.Inject
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.FetchService

import scala.concurrent.{ExecutionContext, Future}

class DefaultQueryController @Inject()(val controllerComponents: ControllerComponents,
                                       val fetchService: FetchService) extends QueryController

trait QueryController extends BaseController with ApiResponse {

  val fetchService: FetchService

  implicit val ec: ExecutionContext = controllerComponents.executionContext

  def getAuditEvents(): Action[AnyContent] = Action.async { implicit req =>
    req.getQueryString("userId") match {
      case Some(userId) => fetchService.getEvents(userId) map { events =>
        withJsonResponseBody(OK, Json.toJson(events)) { json =>
          Ok(json)
        }
      }
      case None => withFutureJsonResponseBody(BAD_REQUEST, JsString("No userId provided")) {
        json => Future.successful(BadRequest(json))
      }
    }
  }
}
