#!/usr/bin/env bash

set -e

./scripts/download_morphology.sh
./gradlew clean makeDatabases
