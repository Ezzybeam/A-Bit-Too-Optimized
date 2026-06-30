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

# Collect the release jars (not the -sources jars) into a single dist/ folder
# so they are easy to find instead of being buried per-version.
echo "==> Collecting release jars into dist/"
mkdir -p dist
rm -f dist/*.jar
find versions -path "*build/libs/*.jar" ! -name "*-sources.jar" -exec cp {} dist/ \;

# Archive every build so older mod versions stay available. Unlike dist/ (which
# only holds the latest), archive/ accumulates and is never cleared. Jars carry
# the Minecraft and mod version in their names, so versions never collide.
echo "==> Archiving jars into archive/"
mkdir -p archive
find versions -path "*build/libs/*.jar" ! -name "*-sources.jar" -exec cp -n {} archive/ \;

echo "==> Done."
echo "Latest jars (dist/):"
ls -1 dist/*.jar
echo "Archived versions (archive/):"
ls -1 archive/*.jar
