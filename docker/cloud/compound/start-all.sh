#!/bin/sh

# Start config service
./app/start.sh config 8888
while ! ./app/check.sh 8888; do
  sleep 1
done

echo "Service config started."

# Start discovery service
./app/start.sh discovery 8761
while ! ./app/check.sh 8761; do
  sleep 1
done

echo "Service discovery started."

# Start gateway service
./app/start.sh gateway 8080

while ! ./app/check.sh 8080; do
  sleep 1
done

echo "Service gateway started."

sleep infinity
