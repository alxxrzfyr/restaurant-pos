package com.restaurant.pos.repository;

import java.util.Map;
import java.util.Optional;

public interface SettingsRepository {

    Optional<String> get(String key);

    Map<String, String> getAll();

    void set(String key, String value);
}
