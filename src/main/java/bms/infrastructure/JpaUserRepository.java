
package bms.infrastructure;

import bms.application.UserRepository;
import bms.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Adapter: implements UserRepository,
// maps to UserEntity and hands the work to Spring Data.
@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springData;

    public JpaUserRepository(SpringDataUserRepository springData) {
        this.springData = springData;
    }

    // 1. Save or update a user
    @Override
    public void save(User user) {
        springData.save(UserEntity.from(user));
    }

    // 2. Get all users
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        for (UserEntity entity : springData.findAll(Sort.by("id"))) {
            users.add(entity.toUser());
        }

        return users;
    }

    // 3. Find a user by ID
    @Override
    public Optional<User> findById(Long id) {
        return springData.findById(id).map(UserEntity::toUser);
    }

    // 4. Check whether a user ID exists
    @Override
    public boolean existsById(Long id) {
        return springData.existsById(id);
    }

    // 5. Check whether an email exists
    @Override
    public boolean existsByEmail(String email) {
        return springData.existsByEmailIgnoreCase(email);
    }

    // 6. Delete a user by ID
    @Override
    public void deleteById(Long id) {
        springData.deleteById(id);
    }
}
