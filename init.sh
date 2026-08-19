#!/usr/bin/env bash
set -e
echo "== Architecture check =="
./check_architecture.sh
echo "== Build =="
./mvnw clean install -q
echo "== Start & health check =="
./mvnw spring-boot:run &
PID=$!
sleep 15
curl -sf http://localhost:8080/actuator/health || (kill $PID; exit 1)
echo "== Feature checks =="
curl -sf -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -d '{"title":"init-check"}'
curl -sf http://localhost:8080/api/tasks | grep -q "init-check"
kill $PID
echo "OK: all checks passed"