package com.restaurant.pos.service;

import com.restaurant.pos.model.AuditEvent;
import com.restaurant.pos.model.AuditEventType;
import com.restaurant.pos.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void record(AuditEventType eventType, Long userId, String username, String details) {
        Instant now = Instant.now();
        String prevHash = auditEventRepository.findLatest().map(AuditEvent::hash).orElse(null);
        String dataToHash = (prevHash != null ? prevHash : "") + "|" + now.toEpochMilli() + "|" + userId + "|" + username + "|" + eventType + "|" + details;
        String hash = computeHash(dataToHash);

        AuditEvent event = AuditEvent.builder()
                .occurredAt(now)
                .userId(userId)
                .username(username)
                .eventType(eventType)
                .details(details)
                .hash(hash)
                .prevHash(prevHash)
                .build();
        auditEventRepository.insert(event);
        log.info("{} user={} details={}", eventType, username, details);
    }

    public List<AuditEvent> findByDateRange(Instant from, Instant to) {
        return auditEventRepository.findByDateRange(from, to);
    }

    private String computeHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute hash", e);
        }
    }
}
