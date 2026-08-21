# Restaurant POS

> An enterprise-grade restaurant point-of-sale desktop application built with Java Swing and FlatLaf.

## Description

**Restaurant POS** is a modern, high-performance point-of-sale system designed specifically for food service operations, cafes, and restaurants. It features a responsive executive dashboard, digital menu management with 86'd stock controls, real-time sales reporting with CSV/Excel/PDF exports, and granular role-based user access controls.

### The Story Behind It

*A bit of history:* This project was originally conceived and built in 2023 as a Grade 11 computer programming project. Recently, it has been fully revived, refactored, and significantly enhanced to meet industry-grade enterprise software standards. This showcases the evolution of my software engineering capabilities, architectural discipline, and attention to user experience design from high school to today.

---

## Features

- **Executive Dashboard:** View live sales volume, net revenue after taxes, active order counts, dynamic sales performance graphs, and recent activity streams without viewport clutter.
- **POS Ordering Terminal:** Food item catalog with category tabs, live cart calculations, tax/discount adjustments, and digital receipt generation.
- **Menu & Catalog Management:** Organize item categories, pricing, unit costs, food images, and instant 86'd availability toggles.
- **Reports & Analytics:** Filter transactions by date range or quick presets (Today, This Week, This Month) and export directly to CSV, Excel, or PDF.
- **Role-Based Security:** Separate roles for Administrators and Cashiers, BCrypt password hashing, rate-limiting, and comprehensive audit logs.
- **Hardware & Settings:** Support for standard and ESC/POS thermal receipt printers, BIR permit registration, and one-click database backups.

---

## Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Java (JDK 21+) |
| **GUI Framework** | Java Swing with [FlatLaf](https://www.formdev.com/flatlaf/) and [MigLayout](http://www.miglayout.com/) |
| **Icons** | Embedded Lucide vector graphics |
| **Persistence** | Embedded SQLite with [Flyway](https://flywaydb.org/) migrations and [HikariCP](https://github.com/brettwooldridge/HikariCP) connection pooling |
| **Security** | BCrypt password hashing and audit logging |
| **Build Tool** | Apache Maven |
| **Packaging** | JPackage self-contained native installers (bundled JRE, no prerequisites) |

---

## Installation & Getting Started

You do **not** need to clone or compile the repository to run Restaurant POS. Choose your preferred installation method below:

### Option 1: Standalone Windows Installer (`.exe`)

For Windows users who want a standard setup wizard:
1. Download the latest **`RestaurantPOS-Setup.exe`** from the [Releases](https://github.com/alxxrzfyr/restaurant-pos/releases) page.
2. Double-click the installer and follow the setup prompts.
3. Launch **Restaurant POS** from your Desktop shortcut or Start Menu.

---

### Option 2: Automated 1-Click Installer Scripts

#### On Linux / macOS:
If you have the distribution archive or repository:
```bash
./install.sh
```
*This verifies your Java 21+ runtime, installs application binaries into `~/.local/share/restaurant-pos`, creates a `restaurant-pos` terminal command, and generates a Linux desktop launcher shortcut.*

#### On Windows:
Double-click `install.bat` or execute in Command Prompt:
```cmd
install.bat
```
*This installs the application to `%LOCALAPPDATA%\RestaurantPOS` and creates a desktop shortcut.*

---

### Option 3: Run Directly from Executable Fat JAR

If you already have Java 21+ installed on your machine:
```bash
java -jar target/app.jar
```

---

### Option 4: Build and Run from Source (Developers)

#### Prerequisites
- **Java Development Kit (JDK):** Version 21 is required. ([Download Eclipse Temurin JDK 21](https://adoptium.net/))
- **Build Tool:** Maven 3.8+ (`mvn -v`). ([Download Maven](https://maven.apache.org/install.html))
- **Database:** SQLite (embedded). No external database server or installation is required!

#### Build Steps
```bash
# 1. Clone repository
git clone https://github.com/alxxrzfyr/restaurant-pos.git
cd restaurant-pos

# 2. Package fat JAR
mvn clean package -DskipTests

# 3. Launch from source
mvn compile exec:java -Dexec.mainClass="com.restaurant.pos.Main"
```

---

## Default Login Credentials

The system automatically initializes an embedded SQLite database (`restaurant-pos.db`) on first launch with the following default accounts:

| Role | Username | Default Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Full Access (Dashboard, Menu, Reports, Users, Settings) |
| **Cashier** | `cashier1` | `cashier123` | POS Terminal, Orders, and Shift History |

> **Security Note:** Navigate to the "Users" or "Settings" section inside the dashboard and change your administrator password immediately after first login.

---

## Usage Guide

1. **First Launch:** Launch the application. SQLite and Flyway will automatically configure the schema without manual database setup.
2. **Executive Dashboard:** View gross and net revenues, top-performing items, sales distribution charts, and recent transaction history.
3. **Menu Management:** Use the sidebar to create food categories and configure menu items with prices, costs, photos, and availability status.
4. **Point of Sale (Cashier):** Open the POS terminal to select items, adjust quantities, select order type (Dine-In or Take-Out), enter table numbers, and process payment.
5. **Reports & Exports:** Filter sales records by date presets (Today, This Week, This Month) and export transaction logs to CSV, Excel, or PDF.
6. **System Configuration:** Configure business TIN, BIR permit details, receipt headers, and trigger one-click database backups from the Settings center.

---

## Troubleshooting & FAQ

| Issue | Likely Cause | Solution |
| :--- | :--- | :--- |
| **`UnsupportedClassVersionError`** | Running an older Java version (e.g. Java 8, 11, or 17). | Install JDK 21+ and set it as your default `JAVA_HOME` / PATH. |
| **`mvn: command not found`** | Maven is not installed or not in system PATH. | Install Apache Maven or run directly using the standalone `.exe` / `app.jar`. |
| **Database locked error** | Another instance of Restaurant POS is already running. | Close any running POS instances in Task Manager / System Monitor before relaunching. |
| **Thermal printer not detected** | Printer driver is offline or disconnected. | Check USB cable connection and configure the printer driver in *Settings > Receipt Printer Setup*. |
| **Corrupted database** | Unsaved crash or system interruption. | Restore your latest backup archive via *Settings > Database Maintenance > Restore Database*. |

---

## Project Structure

```text
restaurant-pos/
├── installer/            # Native packaging scripts (jpackage)
│   └── build-package.sh
├── src/
│   ├── main/java/com/restaurant/pos/
│   │   ├── config/       # Application configuration and settings
│   │   ├── exception/    # Custom domain exceptions
│   │   ├── model/        # Domain entities (Order, User, MenuItem, Category, etc.)
│   │   ├── repository/   # SQLite JDBC repositories
│   │   ├── security/     # BCrypt hashing, session state, rate limiters
│   │   ├── service/      # Core business logic and reporting engines
│   │   ├── ui/           # Swing user interface (FlatLaf, MigLayout, Dialogs)
│   │   └── Main.java     # Application entry point
│   └── main/resources/   # Lucide SVG vector assets, SQL migrations, database schemas
├── install.sh            # Linux & macOS 1-click installer
├── install.bat           # Windows 1-click installer
├── pom.xml               # Maven dependencies and shading configuration
└── README.md             # Project documentation
```

---

## Known Limitations & Future Improvements

- **Local Persistence:** The application uses an embedded SQLite database optimized for single physical terminals. Future iterations may support multi-terminal server synchronization over PostgreSQL.
- **Hardware Integration:** Cash drawer pulse triggering via raw ESC/POS kick codes is supported; future builds will expand support for direct serial/OPOS barcode scanners.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- Built as a passion project to bridge the gap between high school programming and enterprise-grade software engineering.
- Thanks to the open-source community behind [FlatLaf](https://www.formdev.com/flatlaf/) and [MigLayout](http://www.miglayout.com/) for modern desktop UI capabilities.
