#!/usr/bin/env bash
# Build the mod jar for every supported Minecraft version.
# Stonecutter switches the active version per build; we reset to the default
# (26.1.2) at the end so the working tree matches a normal checkout.
#
# Usage: ./build-all.sh
set -euo pipefail
cd "$(dirname "$0")"

VERSIONS=("26.1.2" "26.2")
DEFAULT="26.1.2"

for v in "${VERSIONS[@]}"; do
    echo "==> Building $v"
    ./gradlew "Set active project to $v"
    ./gradlew build
done

echo "==> Resetting active version to $DEFAULT"
./gradlew "Set active project to $DEFAULT"

echo "==> Done. Jars:"
find versions -path "*build/libs/*.jar" ! -name "*-sources.jar"
