
package bms.controller;

import bms.domain.User;
import bms.application.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Handles REST API requests related to user management.
 * Supports creating, viewing, updating, and deleting users. It also forwards HTTP requests to the UserService.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructs the UserController with UserService.
     * @param userService The service used to manage users
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all users.
     * @return A list of all users
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Find a user by ID.
     * @param id The user's ID
     * @return The user with the specified ID
     * @throws ResponseStatusException Indicates that the user does not exist
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {

        return userService.getUserById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found."
                )
            );
    }

    /**
     * Creates a new user.
     * @param user The user information
     * @return The newly created user
     * @throws IllegalArgumentException If user information is invalid
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    /**
     * Updates an existing user.
     * @param id The ID of the user to update
     * @param changes The updated user information
     * @return The updated user
     * @throws IllegalArgumentException If user information is invalid
     * @throws NoSuchElementException If the user does not exist
     */
    @PutMapping("/{id}")
    public User updateUser(
        @PathVariable Long id,
        @RequestBody User changes
    ) {
        return userService.updateUser(id, changes);
    }

    /**
     * Deletes a user by ID.
     * @param id The ID of the user to delete
     * @throws NoSuchElementException If the user does not exist
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    /**
     * Handles invalid user information.
     * Returns HTTP 400 Bad Request.
     * @param exception The validation exception
     * @return An error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(
        IllegalArgumentException exception
    ) {
        return Map.of(
            "error",
            exception.getMessage()
        );
    }

    /**
     * Handles requests for users that do not exist.
     * Returns HTTP 404 Not Found.
     * @param exception The user not found exception
     * @return An error message
     */
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(
        NoSuchElementException exception
    ) {
        return Map.of(
            "error",
            exception.getMessage()
        );
    }
}
