# Restaurant POS

> An enterprise-grade restaurant point-of-sale desktop application built with Java Swing.

## Description

**Restaurant POS** is a robust and modern point-of-sale system designed specifically for restaurant operations. 

*A bit of history:* This project was originally built in 2023 as a Grade 11 computer programming project. Recently, it has been fully revived, refactored, and significantly enhanced to meet enterprise-grade software standards. This showcases the evolution of my programming skills from high school to today.

## Features

- **Order Management:** Quickly take orders, manage carts, apply discounts, void items, and checkout securely.
- **Billing & Receipts:** Automatically calculate totals, handle taxes (VAT), and generate structured digital receipts.
- **Executive Dashboard:** View real-time KPI metrics, daily sales charts, and recent activity streams.
- **Menu Management:** Easily add, edit, or categorize menu items with custom profile images and pricing.
- **Role-Based Access Control:** Distinct roles for Administrators (full access, reports, user management) and Cashiers (order processing). Secure login with rate-limiting and audit logging.
- **Reporting:** Export comprehensive sales and cashier reports directly to CSV or Excel formats.
- **Customizable UI:** A sleek, responsive, and modern Swing interface using FlatLaf for a premium user experience (including dark/light mode support).

## Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Java (JDK 21) |
| **GUI Framework** | Java Swing (enhanced with [FlatLaf](https://www.formdev.com/flatlaf/) & [MigLayout](http://www.miglayout.com/)) |
| **Build Tool** | Apache Maven |
| **Database & Persistence** | Embedded SQLite managed via [Flyway](https://flywaydb.org/) & [HikariCP](https://github.com/brettwooldridge/HikariCP) |
| **Security** | BCrypt password hashing & secure session management |
| **Development Tools** | Eclipse IDE & Visual Studio Code |

## Screenshots & Demo

### Executive Dashboard
![Executive Dashboard](./screenshots/executive_dashboard.png)

### POS Terminal
![POS Terminal](./screenshots/pos_terminal.png)

### Login Screen
![Login Screen](./screenshots/login_screen.png)

## Getting Started

### Prerequisites

- **Java Development Kit (JDK):** Version 21 is required. [Download JDK 21 here](https://adoptium.net/).
- **Build Tool:** Maven 3.8+. To check if it's already installed, open your terminal and run `mvn -v`. If it's missing, [install Maven here](https://maven.apache.org/install.html).
- **Database:** SQLite (embedded). No external database server or installation is required!
- **Recommended IDE (Optional):** [IntelliJ IDEA](https://www.jetbrains.com/idea/), Eclipse, or NetBeans. (This is entirely optional. You can run the app directly via the command line).

### Step 1: Clone the Repository

Open your terminal and clone the repository using Git:

```bash
git clone https://github.com/yourusername/restaurant-pos.git
cd restaurant-pos
```
*(Alternatively, if you don't use Git, you can click **Code > Download ZIP** on GitHub and extract the folder).*

### Step 2: Verify Java Installation

Ensure your system is using the correct Java version by running:

```bash
java -version
```
**Expected Output:** You should see `openjdk version "21.x.x"` (or similar). 
*If you get a "command not found" error, or it shows an older version like Java 8 or 11, please install JDK 21 and ensure it is added to your system's PATH.*

### Step 3: Set Up the Database

Because this project uses **SQLite** (a zero-configuration, file-based database) and **Flyway** (for automatic schema migrations), **no manual database setup is required!** 

The database file (`restaurant-pos.db`) and all necessary tables will automatically be generated in the project root folder the very first time you launch the application.

*(If you ever need to view the raw database configuration, the SQLite driver setup is handled internally within the `DataSourceProvider.java` and `AppConfig.java` classes).*

### Step 4: Build the Project

Use Maven to download dependencies, compile the code, and package the application into a single executable JAR file. Run this command in the project root:

```bash
mvn clean install
```

### Step 5: Run the Application

You can launch the application in a few different ways:

**Option A: Via the compiled JAR file**
```bash
java -jar target/app.jar
```

**Option B: Directly via Maven**
```bash
mvn exec:java -Dexec.mainClass="com.restaurant.pos.Main"
```

**Option C: Via an IDE**
Open the `restaurant-pos` folder in your preferred IDE, locate `src/main/java/com/restaurant/pos/Main.java`, and click **Run**.

### Step 6: First-Time Setup / Login

When the application launches for the first time, the database will initialize. Log in using the default administrator credentials:

- **Username:** `admin`
- **Password:** `admin123`

> **Important:** Please navigate to the "Users" or "Settings" section inside the dashboard and change your password immediately after your first login!

### Troubleshooting

| Problem | Likely Cause | Fix |
| :--- | :--- | :--- |
| **`UnsupportedClassVersionError`** | You are running an older version of Java (e.g., Java 8 or 11). | Install JDK 21 and ensure it's set as your default `java` command. |
| **`mvn: command not found`** | Maven is not installed or not added to your system PATH. | Download and install Maven, then add its `bin` folder to your PATH environment variable. |
| **App won't launch / Blank screen** | Conflicting library versions or a corrupted build folder. | Run `mvn clean install` again to rebuild everything fresh from scratch. |

### Uninstalling / Cleanup

To completely remove the application and all its data, simply delete the `restaurant-pos` folder. Since the embedded SQLite database (`restaurant-pos.db`) is stored locally inside this directory, deleting the folder wipes all local data securely. No leftover background services will remain running!

## Usage Guide

1. **First Launch:** When you run the application for the very first time, the database will be created. Log in using the default admin credentials (you should change these immediately).
2. **Dashboard:** Upon login, Administrators are greeted with the Executive Dashboard, summarizing gross revenue, total orders, and top-selling items.
3. **Menu Setup:** Navigate to the "Menu" section via the sidebar to add categories (e.g., "Mains", "Drinks") and populate your menu items.
4. **Point of Sale (Cashier):** Switch to the "Orders" tab (or log in as a Cashier) to open the POS terminal. Click items to add them to the cart, review the order, and hit "Checkout" to process payment and generate a receipt.
5. **Reports:** At the end of the day, go to the "Reports" section to export a summary of all sales and cashier performance to CSV.

## Project Structure

```text
restaurant-pos/
├── src/
│   ├── main/java/com/restaurant/pos/
│   │   ├── ui/           # Swing UI panels, forms, dialogs, and components
│   │   ├── model/        # Domain entities (Order, User, MenuItem, Category, etc.)
│   │   ├── repository/   # Data Access Objects (SQLite JDBC implementations)
│   │   ├── service/      # Core business logic and application services
│   │   ├── security/     # Authentication, password hashing, and rate limiting
│   │   ├── exception/    # Custom application exceptions
│   │   ├── config/       # Application configuration classes
│   │   └── Main.java     # Application entry point
│   └── main/resources/   # Application icons, images, SQL migrations, and properties
├── pom.xml               # Maven configuration and dependencies
└── README.md             # This file
```

## Known Limitations & Future Improvements

- **UI Responsiveness:** While the UI uses `MigLayout` to handle resizing gracefully, extremely small or unusual screen resolutions may clip some components. It is optimized for standard desktop displays (e.g., 1366x768 and up).
- **Local Storage Only:** The application currently relies on a local SQLite database, meaning it is designed for a single physical terminal. Future improvements could involve migrating to PostgreSQL or MySQL for multi-terminal network synchronization.
- **Testing:** While core services are covered by unit tests, UI integration tests are currently limited.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built as a personal project to bridge the gap between high school learning and enterprise software engineering.
- Thanks to the open-source community behind FlatLaf for making Java Swing look incredibly modern and native.
