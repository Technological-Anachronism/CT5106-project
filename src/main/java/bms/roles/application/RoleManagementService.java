package bms.roles.application;

import bms.application.UserRepository;
import bms.domain.Role;
import bms.domain.User;
import bms.roles.domain.ManagedRole;
import bms.roles.domain.RoleAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

/** Business rules for custom role management and assignment. */
@Service
public class RoleManagementService {
    private final ManagedRoleRepository roleRepository;
    private final UserRepository users;

    public RoleManagementService(ManagedRoleRepository roleRepository, UserRepository users) {
        this.roleRepository = roleRepository;
        this.users = users;
    }

    public List<ManagedRole> getRoles() {
        return roleRepository.findAll();
    }

    public ManagedRole getRole(String code) {
        return roleRepository.findByCode(normalize(code))
                .orElseThrow(() -> new NoSuchElementException("Role not found."));
    }

    @Transactional
    public ManagedRole createRole(ManagedRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role information cannot be empty.");
        }
        rejectSystemRoleCode(role.code());
        if (roleRepository.findByCode(role.code()).isPresent()) {
            throw new IllegalArgumentException("Role code already exists.");
        }
        roleRepository.save(role);
        return role;
    }

    @Transactional
    public ManagedRole updateRole(String code, ManagedRole changes) {
        ManagedRole existing = getRole(code);
        if (changes == null) {
            throw new IllegalArgumentException("Role information cannot be empty.");
        }
        if (!existing.code().equals(changes.code())) {
            throw new IllegalArgumentException("Role code cannot be changed.");
        }
        if (roleRepository.countAssignments(existing.code()) > 0
                && existing.baseRole() != changes.baseRole()) {
            throw new IllegalArgumentException("Cannot change base role while users are assigned.");
        }
        roleRepository.save(changes);
        return changes;
    }

    @Transactional
    public void deleteRole(String code) {
        ManagedRole role = getRole(code);
        // Remove outdated assignments caused by deleted or externally edited users.
        getAssignments();
        if (roleRepository.countAssignments(role.code()) > 0) {
            throw new IllegalArgumentException("Role is assigned to users and cannot be deleted.");
        }
        roleRepository.deleteByCode(role.code());
    }

    @Transactional
    public RoleAssignment assignRole(Long userId, String code) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be empty.");
        }
        ManagedRole role = getRole(code);
        User user = users.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found."));
        user.setRole(role.baseRole());
        users.save(user);
        RoleAssignment assignment = new RoleAssignment(userId, role.code());
        roleRepository.saveAssignment(assignment);
        return assignment;
    }

    /** Clears a custom role, retaining the user's existing base system role. */
    @Transactional
    public void unassignRole(Long userId) {
        if (!users.existsById(userId)) {
            throw new NoSuchElementException("User not found.");
        }
        roleRepository.deleteAssignment(userId);
    }

    /** Drops stale assignments when users were removed or changed via legacy APIs. */
    @Transactional
    public List<RoleAssignment> getAssignments() {
        for (RoleAssignment assignment : roleRepository.findAssignments()) {
            User user = users.findById(assignment.userId()).orElse(null);
            ManagedRole role = roleRepository.findByCode(assignment.roleCode()).orElse(null);
            if (user == null || role == null || user.getRole() != role.baseRole()) {
                roleRepository.deleteAssignment(assignment.userId());
            }
        }
        return roleRepository.findAssignments();
    }

    private static String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Role code cannot be empty.");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static void rejectSystemRoleCode(String code) {
        for (Role value : Role.values()) {
            if (value.name().equals(code)) {
                throw new IllegalArgumentException("System roles cannot be redefined.");
            }
        }
    }
}
