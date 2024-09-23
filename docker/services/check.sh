#!/bin/sh

curl --fail --silent "localhost:$1/actuator/health/readiness" | grep -q UP || exit 1
