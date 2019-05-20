
package helpers

import com.cjwwdev.testing.common.{FutureHelpers, JsonValidation}
import com.cjwwdev.testing.integration.http.ResponseHelpers
import com.cjwwdev.testing.integration.wiremock.{StubbedBasicHttpCalls, WireMockSetup}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait IntegrationSpec
  extends PlaySpec
    with BeforeAndAfter
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with FutureHelpers
    with JsonValidation
    with ResponseHelpers
    with StubbedBasicHttpCalls
    with WireMockSetup {

  implicit val ec: ExecutionContext = global.prepare()

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
  }
}
