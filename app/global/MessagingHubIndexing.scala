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

import com.cjwwdev.mongo.DatabaseRepository
import com.cjwwdev.mongo.indexes.RepositoryIndexer
import database.{AuditEventStore, FeedEventStore}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext => ExC}

class MessagingHubIndexing @Inject()(val feedEventStore: FeedEventStore,
                                     val auditEventStore: AuditEventStore,
                                     implicit val ec: ExC) extends RepositoryIndexer {
  override val repositories: Seq[DatabaseRepository] = Seq(feedEventStore, auditEventStore)
  runIndexing
}
