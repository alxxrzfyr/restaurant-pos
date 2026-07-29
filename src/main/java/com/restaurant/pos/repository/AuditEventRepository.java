package com.restaurant.pos.repository;

import com.restaurant.pos.model.AuditEvent;

import java.time.Instant;
import java.util.List;

public interface AuditEventRepository {

    AuditEvent insert(AuditEvent event);

    List<AuditEvent> findByDateRange(Instant from, Instant to);

    java.util.Optional<AuditEvent> findLatest();
}
