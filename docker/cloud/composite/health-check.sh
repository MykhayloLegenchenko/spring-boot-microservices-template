#!/bin/sh

wget --quiet --tries=1 -O - "http://localhost:$1/actuator/health" | grep -q '"status":"UP"' || exit 1
