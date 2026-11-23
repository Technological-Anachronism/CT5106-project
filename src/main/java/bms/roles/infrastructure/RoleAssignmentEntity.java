package bms.roles.infrastructure;

import bms.roles.domain.RoleAssignment;
import jakarta.persistence.*;

/** Stores the optional custom role assigned to a user. */
@Entity
@Table(name = "user_managed_roles")
public class RoleAssignmentEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    protected RoleAssignmentEntity() { }

    static RoleAssignmentEntity from(RoleAssignment assignment) {
        RoleAssignmentEntity entity = new RoleAssignmentEntity();
        entity.userId = assignment.userId();
        entity.roleCode = assignment.roleCode();
        return entity;
    }

    RoleAssignment toDomain() {
        return new RoleAssignment(userId, roleCode);
    }
}
