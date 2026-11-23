
package bms.controller;

import bms.application.UserService;
import bms.domain.Role;
import bms.domain.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests the User Management REST API.
 * Checks HTTP requests, responses, and error handling.
 */
@WebMvcTest(UserController.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    /**
     * Tests retrieving all users.
     */
    @Test
    void getAllUsersSuccessfully() throws Exception {

        when(userService.getAllUsers()).thenReturn(
            List.of(
                new User(1L, "Alice", "alice@uni.ie", Role.USER),
                new User(2L, "Bob", "bob@uni.ie", Role.ADMIN)
            )
        );

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Alice"))
            .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    /**
     * Tests retrieving a user by ID.
     */
    @Test
    void getUserByIdSuccessfully() throws Exception {

        when(userService.getUserById(1L)).thenReturn(
            Optional.of(
                new User(1L, "Alice", "alice@uni.ie", Role.USER)
            )
        );

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    /**
     * Tests creating a new user.
     */
    @Test
    void createUserSuccessfully() throws Exception {

        when(userService.createUser(any(User.class))).thenReturn(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );

        String json = """
            {
                "id": 1,
                "name": "Alice",
                "email": "alice@uni.ie",
                "role": "USER"
            }
            """;

        mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Alice"));
    }

    /**
     * Tests updating an existing user.
     */
    @Test
    void updateUserSuccessfully() throws Exception {

        when(userService.updateUser(
            eq(1L), any(User.class)
        )).thenReturn(
            new User(1L, "Alice Updated", "new@uni.ie", Role.ADMIN)
        );

        String json = """
            {
                "id": 1,
                "name": "Alice Updated",
                "email": "new@uni.ie",
                "role": "ADMIN"
            }
            """;

        mockMvc.perform(
                put("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Alice Updated"))
            .andExpect(jsonPath("$.email").value("new@uni.ie"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * Tests deleting an existing user.
     */
    @Test
    void deleteUserSuccessfully() throws Exception {

        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isNoContent());
    }

    /**
     * Tests HTTP 404 when a user is not found.
     */
    @Test
    void getMissingUserReturns404() throws Exception {

        when(userService.getUserById(99L)).thenReturn(
            Optional.empty()
        );

        mockMvc.perform(get("/api/users/99"))
            .andExpect(status().isNotFound());
    }

    /**
     * Tests HTTP 400 for invalid user information.
     */
    @Test
    void invalidUserReturns400() throws Exception {

        when(userService.createUser(any(User.class)))
            .thenThrow(
                new IllegalArgumentException(
                    "User name cannot be empty."
                )
            );

        String json = """
            {
                "id": 1,
                "name": "",
                "email": "test@uni.ie",
                "role": "USER"
            }
            """;

        mockMvc.perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                .value("User name cannot be empty."));
    }

    /**
     * Tests HTTP 404 when deleting a missing user.
     */
    @Test
    void deleteMissingUserReturns404() throws Exception {

        doThrow(new NoSuchElementException("User not found."))
            .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                .value("User not found."));
    }
}
