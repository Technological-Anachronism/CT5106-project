package bms.roles.infrastructure;

import bms.domain.Role;
import bms.roles.domain.ManagedRole;
import jakarta.persistence.*;

/** Stores a custom role in PostgreSQL. */
@Entity
@Table(name = "managed_roles")
public class ManagedRoleEntity {
    @Id
    @Column(length = 50, nullable = false)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_role", nullable = false, length = 30)
    private Role baseRole;

    protected ManagedRoleEntity() { }

    static ManagedRoleEntity from(ManagedRole role) {
        ManagedRoleEntity entity = new ManagedRoleEntity();
        entity.code = role.code();
        entity.name = role.name();
        entity.baseRole = role.baseRole();
        return entity;
    }

    ManagedRole toDomain() {
        return new ManagedRole(code, name, baseRole);
    }
}
