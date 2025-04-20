#!/bin/bash

RELEASE_NOTES="${1}"
export RELEASE_NOTES

echo "Release notes set to: $RELEASE_NOTES"

./gradlew assembleRelease
./gradlew githubRelease
