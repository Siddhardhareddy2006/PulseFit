#!/usr/bin/env bash
set +e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT/.run-logs"
for port in 5500 8080 8081 8082 8083 8084 8761; do
  pids=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null)
  if [ -n "$pids" ]; then echo "$pids" | xargs kill 2>/dev/null; fi
done
if [ -d "$LOG_DIR" ]; then
  for f in "$LOG_DIR"/*.pid; do [ -f "$f" ] && kill "$(cat "$f")" 2>/dev/null; done
  rm -f "$LOG_DIR"/*.pid
fi
echo "Stopped PulseFit processes listening on ports 5500, 8080-8084 and 8761."
