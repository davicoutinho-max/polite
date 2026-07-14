#!/usr/bin/env bash
# Run once by the one-shot `kafka-init` service in docker-compose.yml,
# after the broker reports healthy. Idempotent — safe to re-run.
set -euo pipefail

BROKER="${KAFKA_BROKER:-kafka:9092}"
TOPICS_FILE="/topics.txt"

echo ">>> Waiting for Kafka at ${BROKER}..."
until /opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BROKER" --list > /dev/null 2>&1; do
  sleep 2
done
echo ">>> Kafka is reachable, creating topics..."

while IFS=":" read -r name partitions replication; do
  [[ -z "$name" || "$name" == \#* ]] && continue
  echo ">>> ensuring topic '$name' (partitions=$partitions, replication=$replication)"
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BROKER" \
    --create --if-not-exists \
    --topic "$name" \
    --partitions "$partitions" \
    --replication-factor "$replication"
done < "$TOPICS_FILE"

echo ">>> All topics ensured. Current list:"
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BROKER" --list
