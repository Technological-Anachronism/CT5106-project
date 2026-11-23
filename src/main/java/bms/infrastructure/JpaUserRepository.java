
package bms.infrastructure;

import bms.application.UserRepository;
import bms.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implements UserRepository using Spring Data JPA.
 * Converts User objects to UserEntity objects for database operations.
 */
@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springData;

    /**
     * Constructs JpaUserRepository with Spring Data.
     * @param springData The repository used for database access
     */
    public JpaUserRepository(SpringDataUserRepository springData) {
        this.springData = springData;
    }

    /**
     * Saves or updates a user in the database.
     * @param user The user to save
     */
    @Override
    public void save(User user) {
        springData.save(UserEntity.from(user));
    }

    /**
     * Retrieves all users sorted by ID.
     * @return A list of all users
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        for (UserEntity entity : springData.findAll(Sort.by("id"))) {
            users.add(entity.toUser());
        }

        return users;
    }

    /**
     * Finds a user by ID.
     * @param id The user's ID
     * @return The user if found, otherwise an empty Optional
     */
    @Override
    public Optional<User> findById(Long id) {
        return springData.findById(id).map(UserEntity::toUser);
    }

    /**
     * Checks if a user ID exists.
     * @param id The user's ID
     * @return True if the ID exists, otherwise false
     */
    @Override
    public boolean existsById(Long id) {
        return springData.existsById(id);
    }

    /**
     * Checks if an email address exists.
     * @param email The email address to check
     * @return True if the email exists, otherwise false
     */
    @Override
    public boolean existsByEmail(String email) {
        return springData.existsByEmailIgnoreCase(email);
    }

    /**
     * Deletes a user by ID.
     * @param id The ID of the user to delete
     */
    @Override
    public void deleteById(Long id) {
        springData.deleteById(id);
    }
}
