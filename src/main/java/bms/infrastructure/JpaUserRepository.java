package bms.infrastructure;

import bms.application.UserRepository;
import bms.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Adapter: implements UserRepository, maps to UserEntity and hands the work to Spring Data.
@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springData;

    public JpaUserRepository(SpringDataUserRepository springData) {
        this.springData = springData;
    }

    @Override
    public void save(User user) {
        springData.save(UserEntity.from(user));
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        for (UserEntity entity : springData.findAll(Sort.by("id"))) {
            users.add(entity.toUser());
        }
        return users;
    }

    @Override
    public Optional<User> findById(Long id) {
        return springData.findById(id).map(UserEntity::toUser);
    }

    @Override
    public boolean existsById(Long id) {
        return springData.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springData.existsByEmailIgnoreCase(email);
    }
}
