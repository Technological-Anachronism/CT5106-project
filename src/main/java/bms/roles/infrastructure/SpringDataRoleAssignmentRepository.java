package bms.roles.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data access to user-to-custom-role assignments. */
interface SpringDataRoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, Long> {
    long countByRoleCode(String roleCode);
}
