#!/usr/bin/env bash

echo -e "Running unit tests for gode-sunder example model...\n"
cd ./gode-sunder
sbt clean coverage test
echo -e "...completed unit tests.\n"

