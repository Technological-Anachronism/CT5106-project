
package bms.application;

import bms.domain.User;

// A List is used to store multiple users
import java.util.List;

// Optional means query results may or may not exist
import java.util.Optional;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

// User Management:
// 1. Create a user
// 2. Query all users
// 3. Query a user by ID
// 4. Edit a user
// 5. Delete a user
// 6. Validate user information
// 7. Check duplicate IDs and emails

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Check whether user information is valid
    private void validateUser(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                "User information cannot be empty."
            );
        }

        if (user.getId() == null) {
            throw new IllegalArgumentException(
                "User ID cannot be empty."
            );
        }

        if (user.getName() == null ||
            user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "User name cannot be empty."
            );
        }

        if (user.getEmail() == null ||
            user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "User email cannot be empty."
            );
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException(
                "User role cannot be empty."
            );
        }
    }

    // 1. Create User
    public User createUser(User user) {

        validateUser(user);

        if (userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException(
                "User ID already exists."
            );
        }

        if (userRepository.existsByEmail(
                user.getEmail().trim())) {
            throw new IllegalArgumentException(
                "Email already exists."
            );
        }

        // Organize user information
        user.setName(user.getName().trim());

        // Remove spaces before and after email
        user.setEmail(user.getEmail().trim());

        userRepository.save(user);

        return user;
    }

    // 2. Get All Users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 3. Get User By ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // 4. Edit User
    public User updateUser(Long id, User changes) {

        if (changes == null) {
            throw new IllegalArgumentException(
                "User information cannot be empty."
            );
        }

        // User ID cannot be changed
        if (changes.getId() != null &&
            !id.equals(changes.getId())) {
            throw new IllegalArgumentException(
                "User ID cannot be changed."
            );
        }

        // Find existing user
        User existing = userRepository.findById(id)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "User not found."
                )
            );

        changes.setId(id);

        validateUser(changes);

        String newEmail = changes.getEmail().trim();

        // Check if another user already has this email
        if (!existing.getEmail().equalsIgnoreCase(newEmail)
                && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException(
                "Email already exists."
            );
        }

        // Update user information
        existing.setName(changes.getName().trim());
        existing.setEmail(newEmail);
        existing.setRole(changes.getRole());

        // Save updated information
        userRepository.save(existing);

        return existing;
    }

    // 5. Delete User
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException(
                "User not found."
            );
        }

        userRepository.deleteById(id);
    }
}
