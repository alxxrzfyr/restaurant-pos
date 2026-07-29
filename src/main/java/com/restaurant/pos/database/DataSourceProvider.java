package com.restaurant.pos.database;

import com.restaurant.pos.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class DataSourceProvider {

    private static final Logger log = LoggerFactory.getLogger(DataSourceProvider.class);

    private DataSourceProvider() {
    }

    public static HikariDataSource create(AppConfig config, Path dataDirectory) {
        Path dbPath = dataDirectory.resolve(config.dbFile());
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("restaurant-pos-pool");
        hikariConfig.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        hikariConfig.addDataSourceProperty("url", jdbcUrl);
        hikariConfig.addDataSourceProperty("enforceForeignKeys", true);
        hikariConfig.addDataSourceProperty("busyTimeout", config.dbBusyTimeoutMs());
        hikariConfig.addDataSourceProperty("journalMode", "WAL");
        hikariConfig.addDataSourceProperty("synchronous", "NORMAL");

        hikariConfig.setMaximumPoolSize(config.dbPoolMaxSize());
        hikariConfig.setMinimumIdle(config.dbPoolMinIdle());
        hikariConfig.setConnectionTimeout(config.dbConnectionTimeoutMs());

        log.info("Opening SQLite database at {}", dbPath.toAbsolutePath());
        return new HikariDataSource(hikariConfig);
    }
}
