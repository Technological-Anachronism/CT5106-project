
package bms.application;

import bms.domain.Role;
import bms.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService.
 * Tests user creation, retrieval, updating, deleting,
 * and validation.
 */
class UserServiceTest {

    private UserService userService;
    private TestUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TestUserRepository();
        userService = new UserService(repository);
    }

    /**
     * Tests creating a valid user.
     */
    @Test
    void createUserSuccessfully() {
        User user = new User(
            1L, " Test ", " test@uni.ie ", Role.USER
        );

        User created = userService.createUser(user);

        assertEquals("Test", created.getName());
        assertEquals("test@uni.ie", created.getEmail());
        assertTrue(repository.existsById(1L));
    }

    /**
     * Tests rejecting duplicate user IDs.
     */
    @Test
    void rejectDuplicateId() {
        userService.createUser(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );

        assertThrows(IllegalArgumentException.class, () ->
            userService.createUser(
                new User(1L, "Bob", "bob@uni.ie", Role.USER)
            )
        );
    }

    /**
     * Tests rejecting duplicate email addresses.
     */
    @Test
    void rejectDuplicateEmail() {
        userService.createUser(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );

        assertThrows(IllegalArgumentException.class, () ->
            userService.createUser(
                new User(2L, "Bob", "alice@uni.ie", Role.USER)
            )
        );
    }

    /**
     * Tests rejecting missing user information.
     */
    @Test
    void rejectInvalidUser() {
        assertThrows(IllegalArgumentException.class, () ->
            userService.createUser(
                new User(1L, "", "test@uni.ie", Role.USER)
            )
        );

        assertThrows(IllegalArgumentException.class, () ->
            userService.createUser(
                new User(2L, "Test", "", Role.USER)
            )
        );

        assertThrows(IllegalArgumentException.class, () ->
            userService.createUser(null)
        );
    }

    /**
     * Tests retrieving all users.
     */
    @Test
    void getAllUsersSuccessfully() {
        userService.createUser(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );
        userService.createUser(
            new User(2L, "Bob", "bob@uni.ie", Role.ADMIN)
        );

        assertEquals(2, userService.getAllUsers().size());
    }

    /**
     * Tests finding users by ID.
     */
    @Test
    void getUserByIdSuccessfully() {
        userService.createUser(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );

        assertTrue(userService.getUserById(1L).isPresent());
        assertTrue(userService.getUserById(99L).isEmpty());
    }

    /**
     * Tests updating an existing user.
     */
    @Test
    void updateUserSuccessfully() {
        userService.createUser(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );

        User updated = userService.updateUser(
            1L,
            new User(1L, "Alice Updated", "new@uni.ie", Role.ADMIN)
        );

        assertEquals("Alice Updated", updated.getName());
        assertEquals("new@uni.ie", updated.getEmail());
        assertEquals(Role.ADMIN, updated.getRole());
    }

    /**
     * Tests rejecting changes to user IDs.
     */
    @Test
    void rejectIdChange() {
        userService.createUser(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );

        assertThrows(IllegalArgumentException.class, () ->
            userService.updateUser(
                1L,
                new User(2L, "Alice", "alice@uni.ie", Role.USER)
            )
        );
    }

    /**
     * Tests rejecting duplicate emails during updates.
     */
    @Test
    void rejectDuplicateEmailOnUpdate() {
        userService.createUser(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );
        userService.createUser(
            new User(2L, "Bob", "bob@uni.ie", Role.USER)
        );

        assertThrows(IllegalArgumentException.class, () ->
            userService.updateUser(
                2L,
                new User(2L, "Bob", "alice@uni.ie", Role.USER)
            )
        );
    }

    /**
     * Tests rejecting updates to missing users.
     */
    @Test
    void rejectUpdatingMissingUser() {
        assertThrows(NoSuchElementException.class, () ->
            userService.updateUser(
                99L,
                new User(99L, "Test", "test@uni.ie", Role.USER)
            )
        );
    }

    /**
     * Tests deleting an existing user.
     */
    @Test
    void deleteUserSuccessfully() {
        userService.createUser(
            new User(1L, "Alice", "alice@uni.ie", Role.USER)
        );

        userService.deleteUser(1L);

        assertFalse(repository.existsById(1L));
    }

    /**
     * Tests rejecting deletion of missing users.
     */
    @Test
    void rejectDeletingMissingUser() {
        assertThrows(NoSuchElementException.class, () ->
            userService.deleteUser(99L)
        );
    }

    /**
     * A simple repository used only for unit testing.
     * Stores users in memory instead of a database.
     */
    private static class TestUserRepository
            implements UserRepository {

        private final Map<Long, User> users =
            new LinkedHashMap<>();

        @Override
        public void save(User user) {
            users.put(user.getId(), user);
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users.values());
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public boolean existsById(Long id) {
            return users.containsKey(id);
        }

        @Override
        public boolean existsByEmail(String email) {
            return users.values().stream()
                .anyMatch(user ->
                    user.getEmail().equalsIgnoreCase(email)
                );
        }

        @Override
        public void deleteById(Long id) {
            users.remove(id);
        }
    }
}
