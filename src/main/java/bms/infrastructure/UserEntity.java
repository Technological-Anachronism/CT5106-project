package bms.infrastructure;

import bms.domain.Role;
import bms.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

// One row in the users table. "user" is already taken in SQL, so the table is "users".
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected UserEntity() {
    }

    static UserEntity from(User user) {
        UserEntity entity = new UserEntity();
        entity.id = user.getId();
        entity.name = user.getName();
        entity.email = user.getEmail();
        entity.role = user.getRole();
        return entity;
    }

    User toUser() {
        return new User(id, name, email, role);
    }
}
