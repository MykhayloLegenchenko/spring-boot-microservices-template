#!/bin/sh
# shellcheck disable=SC2086

cd "$( dirname "$0" )" || exit

../gradlew bootJar -p ../ || exit

docker compose build $1 || exit
docker compose up $1 -d;
