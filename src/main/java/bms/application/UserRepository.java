
package bms.application;

import bms.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Defines methods for accessing user data.
 * Supports saving, searching for, verifying, and deleting users.
 */
public interface UserRepository {

    /**
     * Saves a user.
     * @param user The user to save
     */
    void save(User user);

    /**
     * Retrieves all users.
     * @return A list of all users
     */
    List<User> findAll();

    /**
     * Finds a user by ID.
     * @param id The user's ID
     * @return The user if found, otherwise an empty Optional
     */
    Optional<User> findById(Long id);

    /**
     * Checks if a user ID exists.
     * @param id The user's ID
     * @return True if the ID exists, otherwise false
     */
    boolean existsById(Long id);

    /**
     * Checks if an email address already exists.
     * @param email The email address to check
     * @return True if the email exists, otherwise false
     */
    boolean existsByEmail(String email);

    /**
     * Deletes a user by ID.
     * @param id The ID of the user to delete
     */
    void deleteById(Long id);

}
