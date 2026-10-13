package bms.application;

import bms.domain.User;
// A List is used to store multiple users
import java.util.List;
// “Optional” means that query results may or may not exist.
import java.util.Optional;
import org.springframework.stereotype.Service;

// Main Functions: 1. Create a new user 2. Query all users 3. Query a user by user ID 4. Check if user information is empty 5. Check if a user ID is duplicated 6. Check if an email address is duplicated
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    // Create User
    public User createUser(User user) {

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

        //Organize User Information
        user.setName(user.getName().trim());
        // Remove the spaces before and after the email address
        user.setEmail(user.getEmail().trim());
        userRepository.save(user);
        return user;
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }
    // Get User By ID
    public Optional<User> getUserById(Long id) {

        return userRepository.findById(id);
    }

}