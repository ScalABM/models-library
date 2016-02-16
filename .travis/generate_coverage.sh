#!/usr/bin/env bash

echo -e "Running unit tests and generating coverage statistics for gode-sunder example model...\n"
cd ./gode-sunder
sbt clean coverage test
sbt coveralls
cd ..
echo -e "...finished running unit tests and generating coverage statistics!\n"

