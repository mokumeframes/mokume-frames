#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."
bash ./gradlew --no-daemon --console=plain build "$@"
printf '\nBuilt JARs are in build/libs/. Install the JAR without -sources in its name.\n'
