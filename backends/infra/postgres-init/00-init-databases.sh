#!/usr/bin/env bash
# Runs once, automatically, the first time the postgres container starts
# with an empty data volume (standard docker-entrypoint-initdb.d behavior).
#
# For every microservice: creates its own role + database (true
# database-per-service isolation, even though they share one Postgres
# instance for local-dev resource economy), then applies the exact same
# schema.sql already validated in docs/db/<service>/schema.sql — no drift
# between what's documented and what's actually running.
set -euo pipefail

SCHEMAS_DIR="/schemas"

# service_id : db_name : role_name : password_env_var
SERVICES=(
  "identity-service:identity_service:identity_service_app:IDENTITY_DB_PASSWORD"
  "directory-service:directory_service:directory_service_app:DIRECTORY_DB_PASSWORD"
  "party-management-service:party_management_service:party_management_service_app:PARTY_MANAGEMENT_DB_PASSWORD"
  "platform-configuration-service:platform_configuration_service:platform_configuration_service_app:PLATFORM_CONFIGURATION_DB_PASSWORD"
  "membership-affiliation-service:membership_affiliation_service:membership_affiliation_service_app:MEMBERSHIP_AFFILIATION_DB_PASSWORD"
  "payments-service:payments_service:payments_service_app:PAYMENTS_DB_PASSWORD"
  "feed-content-service:feed_content_service:feed_content_service_app:FEED_CONTENT_DB_PASSWORD"
  "live-streaming-service:live_streaming_service:live_streaming_service_app:LIVE_STREAMING_DB_PASSWORD"
  "fundraising-service:fundraising_service:fundraising_service_app:FUNDRAISING_DB_PASSWORD"
  "elections-service:elections_service:elections_service_app:ELECTIONS_DB_PASSWORD"
  "participation-service:participation_service:participation_service_app:PARTICIPATION_DB_PASSWORD"
  "messaging-service:messaging_service:messaging_service_app:MESSAGING_DB_PASSWORD"
  "notification-service:notification_service:notification_service_app:NOTIFICATION_DB_PASSWORD"
  "privacy-compliance-service:privacy_compliance_service:privacy_compliance_service_app:PRIVACY_COMPLIANCE_DB_PASSWORD"
  "legislative-service:legislative_service:legislative_service_app:LEGISLATIVE_DB_PASSWORD"
  "activity-feed-service:activity_feed_service:activity_feed_service_app:ACTIVITY_FEED_DB_PASSWORD"
  "analytics-service:analytics_service:analytics_service_app:ANALYTICS_DB_PASSWORD"
  "assistant-service:assistant_service:assistant_service_app:ASSISTANT_DB_PASSWORD"
)

for entry in "${SERVICES[@]}"; do
  IFS=":" read -r service_id db_name role_name password_var <<< "$entry"
  password="${!password_var}"

  echo ">>> [$service_id] creating role '$role_name' and database '$db_name'"

  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    DO \$\$
    BEGIN
      IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${role_name}') THEN
        CREATE ROLE ${role_name} WITH LOGIN PASSWORD '${password}';
      END IF;
    END
    \$\$;

    SELECT 'CREATE DATABASE ${db_name} OWNER ${role_name}'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${db_name}')\gexec
EOSQL

  schema_file="${SCHEMAS_DIR}/${service_id}/schema.sql"
  if [ -f "$schema_file" ]; then
    echo ">>> [$service_id] applying $schema_file"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d "$db_name" -f "$schema_file"
    # schema.sql runs as $POSTGRES_USER, so every object it creates is owned by that superuser —
    # GRANT alone covers DML (SELECT/INSERT/UPDATE/DELETE) but not DDL (ALTER TABLE), which
    # Postgres restricts to the object's owner. Reassign ownership to the service's own role so its
    # future Flyway migrations can alter tables schema.sql already created, not just add new ones.
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d "$db_name" -c "REASSIGN OWNED BY ${POSTGRES_USER} TO ${role_name};"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d "$db_name" -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ${role_name};"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d "$db_name" -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO ${role_name};"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d "$db_name" -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ${role_name};"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" -d "$db_name" -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO ${role_name};"
  else
    echo "!!! [$service_id] no schema.sql found at $schema_file — skipping (database created empty)"
  fi
done

echo ">>> All 14 service databases created and seeded."
