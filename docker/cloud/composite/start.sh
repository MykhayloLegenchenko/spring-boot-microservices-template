#!/bin/sh

echo "Starting service $1..."

./app/run.sh "$1" > "app/logs/$1.log" &
