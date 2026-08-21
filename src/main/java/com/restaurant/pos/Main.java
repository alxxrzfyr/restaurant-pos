package com.restaurant.pos;

import com.restaurant.pos.config.AppConfig;
import com.restaurant.pos.database.DataSourceProvider;
import com.restaurant.pos.database.MigrationRunner;
import com.restaurant.pos.ui.login.LoginFrame;
import com.restaurant.pos.ui.theme.AppTheme;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Path dataDirectory = resolveAppDataDirectory();
        System.setProperty("pos.logDir", dataDirectory.resolve("logs").toString());

        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        Logger log = LoggerFactory.getLogger(Main.class);

        AppTheme.install();

        AppConfig config = new AppConfig();
        Path backupDirectory = dataDirectory.resolve("backups");
        try {
            Files.createDirectories(backupDirectory);
        } catch (Exception e) {
            log.error("Failed to create backup directory", e);
        }

        HikariDataSource dataSource = DataSourceProvider.create(config, dataDirectory);
        MigrationRunner.migrate(dataSource);

        AppContext context = new AppContext(config, dataSource, backupDirectory);
        context.bootstrapService().ensureDefaultAdminExists();

        SwingUtilities.invokeLater(() -> new LoginFrame(context).setVisible(true));
    }

    /**
     * Resolves a per-user, writable directory for the application's database,
     * backups, and logs. Falls back to the current working directory only if
     * the OS-specific user data location cannot be determined, so the app
     * never depends on being launched from a directory the OS considers
     * writable (e.g. Program Files on Windows).
     */
    private static Path resolveAppDataDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase();
        Path base;
        if (os.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            base = localAppData != null && !localAppData.isBlank()
                    ? Paths.get(localAppData, "RestaurantPOS")
                    : Paths.get(System.getProperty("user.home", "."), "RestaurantPOS");
        } else if (os.contains("mac")) {
            base = Paths.get(System.getProperty("user.home", "."), "Library", "Application Support", "RestaurantPOS");
        } else {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            base = xdgDataHome != null && !xdgDataHome.isBlank()
                    ? Paths.get(xdgDataHome, "restaurant-pos")
                    : Paths.get(System.getProperty("user.home", "."), ".local", "share", "restaurant-pos");
        }

        try {
            Files.createDirectories(base);
            return base;
        } catch (Exception e) {
            System.err.println("Could not create app data directory at " + base + ", falling back to current directory: " + e);
            return Path.of(".");
        }
    }
}
