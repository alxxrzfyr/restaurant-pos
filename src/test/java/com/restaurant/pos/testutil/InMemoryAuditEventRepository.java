package com.restaurant.pos.testutil;

import com.restaurant.pos.model.AuditEvent;
import com.restaurant.pos.repository.AuditEventRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory {@link AuditEventRepository} fake for service-layer unit tests. */
public final class InMemoryAuditEventRepository implements AuditEventRepository {

    private final List<AuditEvent> events = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong();

    @Override
    public AuditEvent insert(AuditEvent event) {
        AuditEvent saved = AuditEvent.builder()
                .id(idSequence.incrementAndGet())
                .occurredAt(event.occurredAt())
                .userId(event.userId())
                .username(event.username())
                .eventType(event.eventType())
                .details(event.details())
                .hash(event.hash())
                .prevHash(event.prevHash())
                .build();
        events.add(saved);
        return saved;
    }

    @Override
    public java.util.Optional<AuditEvent> findLatest() {
        if (events.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(events.get(events.size() - 1));
    }

    @Override
    public List<AuditEvent> findByDateRange(Instant from, Instant to) {
        return events.stream().filter(e -> !e.occurredAt().isBefore(from) && e.occurredAt().isBefore(to)).toList();
    }

    public List<AuditEvent> all() {
        return List.copyOf(events);
    }
}
