#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="$SCRIPT_DIR/dist"
mkdir -p "$DIST_DIR"

echo "Building fat JAR with Maven..."
(cd "$SCRIPT_DIR" && mvn clean package -DskipTests)

echo "Packaging standalone bundle with jpackage..."
if command -v jpackage &> /dev/null; then
    jpackage \
        --input "$SCRIPT_DIR/target" \
        --name "RestaurantPOS" \
        --main-jar "app.jar" \
        --main-class "com.restaurant.pos.Main" \
        --type app-image \
        --dest "$DIST_DIR" \
        --app-version "1.0.0" \
        --vendor "Restaurant POS Enterprise"
    echo "Application image created at: $DIST_DIR/RestaurantPOS"
else
    echo "jpackage CLI not found; executable JAR is at $SCRIPT_DIR/target/app.jar"
fi
