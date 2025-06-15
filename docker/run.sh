#!/bin/sh

cd "$( dirname "$0" )" || exit

../gradlew assemble -p ../ || exit

# shellcheck disable=SC2086
docker compose build $1 || exit

# shellcheck disable=SC2086
docker compose up $1 -d;
