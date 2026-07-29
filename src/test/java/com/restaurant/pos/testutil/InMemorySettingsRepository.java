package com.restaurant.pos.testutil;

import com.restaurant.pos.repository.SettingsRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory {@link SettingsRepository} fake for service-layer unit tests. */
public final class InMemorySettingsRepository implements SettingsRepository {

    private final Map<String, String> values = new LinkedHashMap<>();

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(values.get(key));
    }

    @Override
    public Map<String, String> getAll() {
        return Map.copyOf(values);
    }

    @Override
    public void set(String key, String value) {
        values.put(key, value);
    }
}
