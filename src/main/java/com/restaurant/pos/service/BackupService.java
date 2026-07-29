package com.restaurant.pos.service;

import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.model.AuditEventType;

import java.nio.file.Path;
import java.sql.Statement;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;

public final class BackupService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    private final ConnectionManager connectionManager;
    private final Path backupDirectory;
    private final AuditService auditService;

    public BackupService(ConnectionManager connectionManager, Path backupDirectory, AuditService auditService) {
        this.connectionManager = connectionManager;
        this.backupDirectory = backupDirectory;
        this.auditService = auditService;
    }

    public Path backupNow(long actingUserId, String actingUsername) {
        String fileName = "restaurant-pos-" + TIMESTAMP_FORMAT.format(Instant.now()) + ".db";
        Path backupFile = backupDirectory.resolve(fileName);

        connectionManager.execute(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("backup to " + quote(backupFile));
                return null;
            }
        });

        auditService.record(AuditEventType.BACKUP_CREATED, actingUserId, actingUsername, backupFile.toString());
        return backupFile;
    }

    public void restore(Path backupFile, long actingUserId, String actingUsername) {
        if (backupFile == null || !java.nio.file.Files.isRegularFile(backupFile)) {
            throw new IllegalArgumentException("Backup file does not exist or is not a valid file: " + backupFile);
        }
        connectionManager.execute(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("restore from " + quote(backupFile));
                return null;
            }
        });

        auditService.record(AuditEventType.BACKUP_RESTORED, actingUserId, actingUsername, backupFile.toString());
    }

    private static String quote(Path path) {
        String value = path.toAbsolutePath().toString();
        if (value.contains("\"")) {
            throw new IllegalArgumentException("Backup path must not contain a double-quote character: " + value);
        }
        return "\"" + value + "\"";
    }
}
