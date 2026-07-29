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

public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private Main() {
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        AppTheme.install();

        AppConfig config = new AppConfig();
        Path dataDirectory = Path.of(".");
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
}
