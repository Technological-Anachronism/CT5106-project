package bms.controller;

import bms.domain.User;

import bms.application.UserService;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


// @RestController indicates that this is a REST API controller responsible for receiving HTTP requests sent by browsers or other programs
@RestController
// All user management APIs begin with /api/users
@RequestMapping("/api/users")
public class UserController {

    // Connect to UserService
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // View All Users (GET)
    @GetMapping
    public List<User> getAllUsers() {

        return userService.getAllUsers();
    }

    //Query a User by ID (GET)
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {

        // If the user does not exist, return an HTTP 404 response.
        return userService.getUserById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found."
            ));
    }

    // Create a New User (POST)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {

        return userService.createUser(user);
    }

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
}