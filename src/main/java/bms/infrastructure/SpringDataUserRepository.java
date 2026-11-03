package bms.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data writes the SQL, including existsByEmailIgnoreCase from the method name.
interface SpringDataUserRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
