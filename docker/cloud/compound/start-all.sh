#!/bin/sh

./app/start.sh discovery 8761
./app/start.sh config 8888

while ! ./app/check.sh 8761; do
  sleep 1
done

while ! ./app/check.sh 8888; do
  sleep 1
done

echo "Services discovery and config started."

./app/start.sh gateway 8080

while ! ./app/check.sh 8080; do
  sleep 1
done

echo "Service gateway started."

sleep infinity
