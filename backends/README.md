# CivicPulse backends

Fourteen microservices, hexagonal architecture, one database per service.
See `docs/architecture/system-architecture.html` for the full design and
`docs/architecture/data-architecture.html` for every table.

## Layout

```
backends/
  docker-compose.yml       # all local-dev infrastructure (see below)
  .env.example             # copy to .env before first run
  infra/
    postgres-init/         # creates the 14 databases + roles, applies schema.sql
    kafka-init/             # creates the ~46 domain-event topics
  identity-service/         # (built first — see repo root TODO in conversation)
  directory-service/
  ...one folder per service in docs/db/
```

## 1. Start the infrastructure

```bash
cd backends
cp .env.example .env        # edit passwords if you want, defaults are fine for local dev
docker compose up -d
```

This brings up:

| Service | Port(s) | What it's for |
|---|---|---|
| **postgres** | 5432 | One Postgres 16 instance, **14 isolated databases** (one per microservice, each with its own role/credentials) — see `infra/postgres-init/00-init-databases.sh`. Every database is seeded from the exact `docs/db/<service>/schema.sql` already validated against PostgreSQL's own grammar. |
| **pgadmin** | 5050 | Web UI to browse all 14 databases. Log in with `PGADMIN_EMAIL`/`PGADMIN_PASSWORD` from `.env`, then add a server pointing at host `postgres`, port `5432`, user `postgres`. |
| **redis** | 6379 | Shared cache/presence store (feed timelines, live-stream viewer counts, typing indicators — see each service's `Redis` notes in the data dossier). Password-protected (`REDIS_PASSWORD`). |
| **kafka** | 9094 (external), 9092 (in-network) | Single-broker KRaft cluster (no Zookeeper). `kafka-init` creates all ~46 domain-event topics from `infra/kafka-init/topics.txt` on first boot and then exits — that's expected (`docker compose ps` will show it as exited, not unhealthy). Internal topics (`__consumer_offsets`, `__transaction_state`) are pinned to replication factor 1 (`KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR` etc. in docker-compose.yml) — **do not remove these**: the default of 3 can never succeed on a single-broker cluster, and every `@KafkaListener` consumer group silently never receives a single message until this is fixed (subscribes fine, but coordinator/offset-commit machinery is broken — no error is thrown, it just never delivers). If you ever see this happen again, `docker exec civicpulse-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list` and check for an endless `Sent auto-creation request for Set(__consumer_offsets)` loop in `docker logs civicpulse-kafka`. |
| **kafka-ui** | 8090 | Web UI to browse topics/messages at http://localhost:8090. |
| **elasticsearch** | 9200 | Directory Service's search index. Basic auth: `elastic` / `ELASTIC_PASSWORD`. |
| **minio** | 9000 (API), 9001 (console) | S3-compatible object storage for avatars, post images, LGPD data exports. Console at http://localhost:9001, login with `MINIO_ROOT_USER`/`MINIO_ROOT_PASSWORD`. |

Verify everything is healthy:

```bash
docker compose ps
```

Every long-running service should show `(healthy)`. `kafka-init` is a one-shot
job — it's expected to show `Exited (0)` once topic creation finishes.

## 2. Database-per-service, for real

Each service connects with **its own** role, which only has privileges on
**its own** database — not the other 13:

| Service | Database | Role |
|---|---|---|
| Identity & Access | `identity_service` | `identity_service_app` |
| Directory | `directory_service` | `directory_service_app` |
| Party Management | `party_management_service` | `party_management_service_app` |
| Platform Configuration | `platform_configuration_service` | `platform_configuration_service_app` |
| Membership & Affiliation | `membership_affiliation_service` | `membership_affiliation_service_app` |
| Payments & Billing | `payments_service` | `payments_service_app` |
| Feed & Content | `feed_content_service` | `feed_content_service_app` |
| Live Streaming | `live_streaming_service` | `live_streaming_service_app` |
| Fundraising | `fundraising_service` | `fundraising_service_app` |
| Elections | `elections_service` | `elections_service_app` |
| Participation | `participation_service` | `participation_service_app` |
| Messaging | `messaging_service` | `messaging_service_app` |
| Notification | `notification_service` | `notification_service_app` |
| Privacy & Compliance | `privacy_compliance_service` | `privacy_compliance_service_app` |

Passwords come from `.env` (one `<SERVICE>_DB_PASSWORD` variable per row above).

Two tables are genuinely partitioned (not just documented as such):
`feed_content_service.posts` (RANGE by `created_at`, with a bootstrap
`posts_default` partition — a real scheduled job using `pg_partman` or
equivalent should roll monthly partitions ahead of need in production) and
`messaging_service.messages` (HASH by `conversation_id`, all 32 buckets
created upfront). `notification_service.notifications` follows the same
RANGE + bootstrap-default pattern as `posts`.

## 3. Resetting everything

```bash
docker compose down -v   # -v also drops the volumes — all data is gone, next `up` re-seeds from scratch
```

If you ever `docker compose up --force-recreate kafka` on its own (without `-v`), the KRaft
broker has been observed to come back up with the `kafkadata` volume's log segments present but
every topic gone except the freshly-recreated internal ones — a one-broker KRaft quirk, not data
loss you need to chase further. Just re-run `docker compose up --force-recreate kafka-init` to
recreate all ~46 topics again (idempotent, `--if-not-exists`).

## 4. Adding a new backend service

Each service folder is a standalone Spring Boot (Java 21) Maven project
following the hexagonal layout documented in
`docs/architecture/system-architecture.html#hexagonal`. Its
`application.yml` should point at the database/role pair from the table
above and at `kafka:9092` / `redis:6379` / `elasticsearch:9200` (service
names resolve inside the `civicpulse_default` Docker network created by
this compose file — from your host machine use `localhost` with the
published ports instead).

Backend service ports run 8081, 8082, 8083... sequentially per service —
**except 8090**, which is already claimed by `kafka-ui`'s published port
(see the table above) and will fail to bind if reused. elections-service
was the 10th service and took `8091` instead of `8090` for this reason;
keep skipping 8090 for any future service in the sequence.
