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

package utils

import models.receipts.Receipt

object Implicits {
  implicit class IntOps(int: Int) {
    def toRange: Range = 0 until int
  }

  implicit class IndexedSeqOps[A <: Receipt](indexedSeq: IndexedSeq[A]) {
    def toReceipt: Receipt = {
      indexedSeq.fold[Receipt](Receipt.emptyReceive) { (build, next) =>
        Receipt(
          fetchedMessages  = build.fetchedMessages  + next.fetchedMessages,
          acceptedMessages = build.acceptedMessages + next.acceptedMessages,
          rejectedMessages = build.rejectedMessages + next.rejectedMessages
        )
      }
    }
  }
}
