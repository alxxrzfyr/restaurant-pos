package com.restaurant.pos.repository;

import com.restaurant.pos.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    User insert(User user);

    void update(User user);
}
