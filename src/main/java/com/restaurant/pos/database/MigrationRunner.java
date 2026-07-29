package com.restaurant.pos.database;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public final class MigrationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationRunner.class);

    private MigrationRunner() {
    }

    public static void migrate(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.repair();
        var result = flyway.migrate();
        log.info("Flyway migration complete: {} migration(s) applied", result.migrationsExecuted);
    }
}
