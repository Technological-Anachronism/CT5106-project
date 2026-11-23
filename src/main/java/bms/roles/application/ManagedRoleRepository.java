package bms.roles.application;

import bms.roles.domain.ManagedRole;
import bms.roles.domain.RoleAssignment;
import java.util.List;
import java.util.Optional;

/** Persistence boundary for custom roles and their user assignments. */
public interface ManagedRoleRepository {
    List<ManagedRole> findAll();
    Optional<ManagedRole> findByCode(String code);
    void save(ManagedRole role);
    void deleteByCode(String code);
    List<RoleAssignment> findAssignments();
    Optional<RoleAssignment> findAssignment(Long userId);
    long countAssignments(String roleCode);
    void saveAssignment(RoleAssignment assignment);
    void deleteAssignment(Long userId);
}
