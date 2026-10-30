#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT"
export DB_USERNAME="${DB_USERNAME:-postgres}" DB_PASSWORD="${DB_PASSWORD:-postgres123}"
mkdir -p .run-logs
SERVER_PORT=8085 EUREKA_INSTANCE_INSTANCE_ID=member-service-2 mvn -pl member-service spring-boot:run >.run-logs/member-2.log 2>&1 & echo $! >.run-logs/member-2.pid
SERVER_PORT=8086 EUREKA_INSTANCE_INSTANCE_ID=subscription-service-2 mvn -pl subscription-service spring-boot:run >.run-logs/subscription-2.log 2>&1 & echo $! >.run-logs/subscription-2.pid
echo "Started extra Member (8085) and Subscription (8086) instances. Check Eureka after 20-30 seconds."
