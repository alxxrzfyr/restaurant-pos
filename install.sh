#!/usr/bin/env bash
set -e

echo "=================================================="
echo "      Restaurant POS - Application Installer     "
echo "=================================================="
echo ""

# 1. Check Java 21+ requirement
echo "[1/4] Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in your PATH."
    echo "Please install Java 21 or higher:"
    echo "  - Ubuntu/Debian: sudo apt install openjdk-21-jre"
    echo "  - Fedora/RHEL:   sudo dnf install java-21-openjdk"
    echo "  - macOS:         brew install openjdk@21"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 21 ] 2>/dev/null; then
    echo "WARNING: Java version detected is $JAVA_VER. Java 21+ is recommended."
fi
echo "Java runtime detected: OK"

# 2. Build or locate fat JAR
echo "[2/4] Verifying executable package..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="$SCRIPT_DIR/target/app.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "Building application JAR using Maven..."
    if command -v mvn &> /dev/null; then
        (cd "$SCRIPT_DIR" && mvn clean package -DskipTests)
    else
        echo "ERROR: Maven is required to compile the package. Please run: sudo apt install maven"
        exit 1
    fi
fi

# 3. Create install directory
echo "[3/4] Installing application files..."
INSTALL_DIR="$HOME/.local/share/restaurant-pos"
BIN_DIR="$HOME/.local/bin"

mkdir -p "$INSTALL_DIR"
mkdir -p "$BIN_DIR"
mkdir -p "$HOME/.local/share/applications"

cp "$JAR_PATH" "$INSTALL_DIR/app.jar"

# Copy sample images or assets if present
if [ -d "$SCRIPT_DIR/sample-images" ]; then
    cp -r "$SCRIPT_DIR/sample-images" "$INSTALL_DIR/"
fi

# Create launcher executable
cat << 'EOF' > "$BIN_DIR/restaurant-pos"
#!/usr/bin/env bash
APP_DIR="$HOME/.local/share/restaurant-pos"
cd "$APP_DIR"
exec java -jar "$APP_DIR/app.jar" "$@"
EOF
chmod +x "$BIN_DIR/restaurant-pos"

# 4. Create desktop application entry (Linux)
echo "[4/4] Creating desktop shortcuts..."
DESKTOP_FILE="$HOME/.local/share/applications/restaurant-pos.desktop"
cat << EOF > "$DESKTOP_FILE"
[Desktop Entry]
Name=Restaurant POS
Comment=Enterprise Restaurant Point of Sale System
Exec=$BIN_DIR/restaurant-pos
Terminal=false
Type=Application
Categories=Office;Finance;
EOF
chmod +x "$DESKTOP_FILE"

echo ""
echo "=================================================="
echo "        Installation Completed Successfully!      "
echo "=================================================="
echo ""
echo "You can launch the application by:"
echo "  1. Running the command: restaurant-pos"
echo "  2. Or launching 'Restaurant POS' from your Application Menu"
echo "  3. Or executing: java -jar target/app.jar"
echo ""
