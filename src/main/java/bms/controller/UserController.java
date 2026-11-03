
package bms.controller;

import bms.domain.User;
import bms.application.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// REST API controller for User Management
@RestController
@RequestMapping("/api/users")
public class UserController {

    // Connect to UserService
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. View all users (GET)
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // 2. Query a user by ID (GET)
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

    // 3. Create a new user (POST)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // 4. Edit an existing user (PUT)
    @PutMapping("/{id}")
    public User updateUser(
        @PathVariable Long id,
        @RequestBody User changes
    ) {
        return userService.updateUser(id, changes);
    }

    // 5. Delete a user (DELETE)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // Handle invalid user information
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

    // Handle user not found
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
