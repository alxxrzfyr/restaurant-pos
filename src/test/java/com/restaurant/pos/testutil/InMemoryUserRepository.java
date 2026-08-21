package com.restaurant.pos.testutil;

import com.restaurant.pos.model.User;
import com.restaurant.pos.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> usersById = new LinkedHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return usersById.values().stream().filter(u -> u.username().equals(username)).findFirst();
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(usersById.values());
    }

    @Override
    public User insert(User user) {
        User saved = user.toBuilder().id(idSequence.incrementAndGet()).build();
        usersById.put(saved.id(), saved);
        return saved;
    }

    @Override
    public void update(User user) {
        usersById.put(user.id(), user);
    }
}
