#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
export DB_USERNAME="${DB_USERNAME:-postgres}"
export DB_PASSWORD="${DB_PASSWORD:-postgres123}"
LOG_DIR="$ROOT/.run-logs"
mkdir -p "$LOG_DIR"

start_service() {
  local name="$1" module="$2"
  echo "Starting ${name}..."
  (cd "$ROOT" && mvn -q -pl "$module" spring-boot:run) >"$LOG_DIR/${name}.log" 2>&1 &
  echo $! > "$LOG_DIR/${name}.pid"
}
wait_url() {
  local url="$1" name="$2" tries="${3:-90}"
  for ((i=1; i<=tries; i++)); do
    if curl -fsS "$url" >/dev/null 2>&1; then echo "$name is responding."; return 0; fi
    sleep 2
  done
  echo "Timed out waiting for $name. Check $LOG_DIR" >&2
  return 1
}

if lsof -nP -iTCP:8761 -sTCP:LISTEN >/dev/null 2>&1; then echo "Port 8761 is already in use. Stop the old PulseFit process first."; exit 1; fi
start_service eureka eureka-server
wait_url http://localhost:8761/actuator/health "Eureka" 60
start_service auth auth-service
start_service member member-service
start_service subscription subscription-service
start_service attendance attendance-service
wait_url http://localhost:8081/actuator/health "Auth Service" 90
wait_url http://localhost:8082/actuator/health "Member Service" 90
wait_url http://localhost:8083/actuator/health "Subscription Service" 90
wait_url http://localhost:8084/actuator/health "Attendance Service" 90
start_service gateway api-gateway
wait_url http://localhost:8080/actuator/health "API Gateway" 60
(cd "$ROOT/frontend" && python3 -m http.server 5500) >"$LOG_DIR/frontend.log" 2>&1 &
echo $! > "$LOG_DIR/frontend.pid"
echo
echo "PulseFit has started. Open http://localhost:5500"
echo "Eureka dashboard: http://localhost:8761"
echo "Logs: $LOG_DIR"
echo "Stop everything with: bash scripts/stop-local.sh"
