
package bms.application;

import bms.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

/**
 * Provides business logic for user management.
* Supports creating, querying, updating, and deleting users. 
* Requires validation of user information and checks for duplicate user IDs and email addresses.
* Accesses user data through the UserRepository and adheres to the “Clean Architecture” principle.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * Constructs the UserService with a user repository.
     * @param userRepository repository used to access user data
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validate the user's required information.
     * Ensure that the user object, ID, name, email, and role are not null.
     * Checks user information.
     *
     * @param user The user to check
     * @throws IllegalArgumentException If user information is invalid
     */
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

    /**
     *Create a new user after validating the information.
     * Check for duplicate IDs and email addresses,
     * Remove extra spaces from the name and email address, and save the user information to the repository.
     *
     * @param user The user to be created
     * @return The newly created user
     * @throws IllegalArgumentException If the user information is invalid or duplicated
     */
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

        user.setName(user.getName().trim());

        user.setEmail(user.getEmail().trim());

        userRepository.save(user);

        return user;
    }

    /**
     * Retrieves all users stored in the repository.
     * @return a list containing all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieve the user based on their unique ID.
     * @param id The user's ID
     * @return Returns an Optional containing the user if found; returns an empty Optional if the user does not exist
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Update an existing user's information.
     * The user ID cannot be changed.
     * Check whether the email address is already in use by another user.
     * @param id The ID of the user to be updated
     * @param changes The user information to be updated
     * @return The updated user
     * @throws IllegalArgumentException if the information is invalid or duplicated
     * @throws NoSuchElementException if the user does not exist
     */
    public User updateUser(Long id, User changes) {

        if (changes == null) {
            throw new IllegalArgumentException(
                "User information cannot be empty."
            );
        }

        if (changes.getId() != null &&
            !id.equals(changes.getId())) {
            throw new IllegalArgumentException(
                "User ID cannot be changed."
            );
        }

        User existing = userRepository.findById(id)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "User not found."
                )
            );

        changes.setId(id);

        validateUser(changes);

        String newEmail = changes.getEmail().trim();

        if (!existing.getEmail().equalsIgnoreCase(newEmail)
                && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException(
                "Email already exists."
            );
        }

        existing.setName(changes.getName().trim());
        existing.setEmail(newEmail);
        existing.setRole(changes.getRole());

        userRepository.save(existing);

        return existing;
    }

    /**
     * Deletes an existing user by user ID.
     * Before deleting, this method verifies whether the specified user exists in the repository.
     * @param id The ID of the user to be deleted
     * @throws NoSuchElementException if the user does not exist
     */
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException(
                "User not found."
            );
        }

        userRepository.deleteById(id);
    }
}
