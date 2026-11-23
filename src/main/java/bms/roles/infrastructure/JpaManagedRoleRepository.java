package bms.roles.infrastructure;

import bms.roles.application.ManagedRoleRepository;
import bms.roles.domain.ManagedRole;
import bms.roles.domain.RoleAssignment;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/** JPA adapter for role and assignment persistence. */
@Repository
public class JpaManagedRoleRepository implements ManagedRoleRepository {
    private final SpringDataManagedRoleRepository roles;
    private final SpringDataRoleAssignmentRepository assignments;

    public JpaManagedRoleRepository(SpringDataManagedRoleRepository roles,
                                    SpringDataRoleAssignmentRepository assignments) {
        this.roles = roles;
        this.assignments = assignments;
    }

    @Override
    public List<ManagedRole> findAll() {
        return roles.findAll(Sort.by("code")).stream().map(ManagedRoleEntity::toDomain).toList();
    }

    @Override
    public Optional<ManagedRole> findByCode(String code) {
        return roles.findById(code).map(ManagedRoleEntity::toDomain);
    }

    @Override
    public void save(ManagedRole role) {
        roles.save(ManagedRoleEntity.from(role));
    }

    @Override
    public void deleteByCode(String code) {
        roles.deleteById(code);
    }

    @Override
    public List<RoleAssignment> findAssignments() {
        return assignments.findAll(Sort.by("userId")).stream()
                .map(RoleAssignmentEntity::toDomain).toList();
    }

    @Override
    public Optional<RoleAssignment> findAssignment(Long userId) {
        return assignments.findById(userId).map(RoleAssignmentEntity::toDomain);
    }

    @Override
    public long countAssignments(String roleCode) {
        return assignments.countByRoleCode(roleCode);
    }

    @Override
    public void saveAssignment(RoleAssignment assignment) {
        assignments.save(RoleAssignmentEntity.from(assignment));
    }

    @Override
    public void deleteAssignment(Long userId) {
        if (assignments.existsById(userId)) {
            assignments.deleteById(userId);
        }
    }
}
