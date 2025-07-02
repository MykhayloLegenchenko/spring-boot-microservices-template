#!/bin/sh
# shellcheck disable=SC2086

trap 'echo "Stopping services..."; pkill -TERM -P $$; wait; echo "Stoped"' TERM

# shellcheck disable=SC3043
start_service() {
  local SERVICE=$1
  local PORT=$2
  local PROFILES=$3

  echo "Starting $SERVICE service on port $PORT..."
  java $JAVA_OPTS -Dspring.profiles.active="$PROFILES" -jar ${SERVICE}.jar > logs/${SERVICE}.log 2>&1 &
  SERVICE_PID=$!

  while ! ./health-check.sh "$PORT"; do
    if ! kill -0 "$SERVICE_PID" 2>/dev/null; then
      echo "❌ Failed"
      exit 1
    fi
    sleep 1
  done

  echo "✅ Started"
}

start_service "config" 8888 "${SPRING_PROFILES_ACTIVE}, no-config"
start_service "discovery" 8761 $SPRING_PROFILES_ACTIVE
start_service "gateway" 8080 $SPRING_PROFILES_ACTIVE

CHILD_TERM_RECEIVED=0
trap 'CHILD_TERM_RECEIVED=1' CHLD
wait

if [ "$CHILD_TERM_RECEIVED" -eq 1 ]; then
  echo "A service exited, stopping the rest..."
  pkill -TERM -P $$
  wait

  echo "Stoped"
fi
