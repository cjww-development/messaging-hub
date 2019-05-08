
package helpers

import com.cjwwdev.testing.integration.IntegrationTestSpec
import com.cjwwdev.testing.integration.application.IntegrationApplication

trait IntegrationSpec extends IntegrationTestSpec with IntegrationApplication {
  override val currentAppBaseUrl: String   = ""
  override val appConfig: Map[String, Any] = Map(
    "database.DefaultFeedEventStore.database"    -> "test-messaging-hub-db",
    "database.DefaultFeedEventStore.collection"  -> "test-feed-event-store",
    "database.DefaultAuditEventStore.database"   -> "test-messaging-hub-db",
    "database.DefaultAuditEventStore.collection" -> "test-audit-event-store"
  )
}
