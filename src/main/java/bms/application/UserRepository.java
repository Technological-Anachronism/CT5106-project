package bms.application;

import bms.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    void save(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    // Delete user by ID
    void deleteById(Long id);

}
