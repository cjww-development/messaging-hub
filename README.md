[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[ ![Download](https://api.bintray.com/packages/cjww-development/releases/messaging-hub/images/download.svg) ](https://bintray.com/cjww-development/releases/messaging-hub/_latestVersion)


# messaging-hub

### About
Messaging hub ferries messages from a configured RabbitMQ queue and saves them into a mongo collection based on a message type.
- `FEED_EVENT` messages are saved to the `feed-event-store`
- `AUDIT_EVENT` messages are saved to the `audit-event-store`

Messages saved as feed events are user facing and can be viewed from the users dashboard on the auth-service.

Messages saved as audit events are **not** user facing and can only be viewed from the event store on administration-frontend.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")