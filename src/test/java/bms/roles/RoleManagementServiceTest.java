package bms.roles;

import bms.application.UserRepository;
import bms.domain.Role;
import bms.domain.User;
import bms.roles.application.ManagedRoleRepository;
import bms.roles.application.RoleManagementService;
import bms.roles.domain.ManagedRole;
import bms.roles.domain.RoleAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Tests role CRUD, validation and assignment without a database. */
class RoleManagementServiceTest {
    private final Map<String, ManagedRole> roles = new LinkedHashMap<>();
    private final Map<Long, RoleAssignment> assignments = new HashMap<>();
    private final Map<Long, User> users = new HashMap<>();
    private RoleManagementService service;

    @BeforeEach
    void setUp() {
        ManagedRoleRepository roleRepository = new ManagedRoleRepository() {
            public List<ManagedRole> findAll() { return new ArrayList<>(roles.values()); }
            public Optional<ManagedRole> findByCode(String code) { return Optional.ofNullable(roles.get(code)); }
            public void save(ManagedRole role) { roles.put(role.code(), role); }
            public void deleteByCode(String code) { roles.remove(code); }
            public List<RoleAssignment> findAssignments() { return new ArrayList<>(assignments.values()); }
            public Optional<RoleAssignment> findAssignment(Long userId) { return Optional.ofNullable(assignments.get(userId)); }
            public long countAssignments(String code) { return assignments.values().stream().filter(a -> a.roleCode().equals(code)).count(); }
            public void saveAssignment(RoleAssignment a) { assignments.put(a.userId(), a); }
            public void deleteAssignment(Long userId) { assignments.remove(userId); }
        };
        UserRepository userRepository = new UserRepository() {
            public void save(User user) { users.put(user.getId(), user); }
            public List<User> findAll() { return new ArrayList<>(users.values()); }
            public Optional<User> findById(Long id) { return Optional.ofNullable(users.get(id)); }
            public boolean existsById(Long id) { return users.containsKey(id); }
            public boolean existsByEmail(String email) { return users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email)); }
            public void deleteById(Long id) { users.remove(id); }
        };
        service = new RoleManagementService(roleRepository, userRepository);
        users.put(1L, new User(1L, "Alice", "alice@uni.ie", Role.USER));
    }

    @Test
    void createsRoleAndNormalizesCode() {
        var role = service.createRole(new ManagedRole("support_agent", "Support Agent", Role.STAFF));
        assertEquals("SUPPORT_AGENT", role.code());
        assertEquals(1, service.getRoles().size());
    }

    @Test
    void rejectsDuplicateCode() {
        service.createRole(new ManagedRole("HELPER", "Helper", Role.USER));
        assertThrows(IllegalArgumentException.class, () ->
                service.createRole(new ManagedRole("HELPER", "Another", Role.STAFF)));
    }

    @Test
    void protectsBuiltInRoleCodes() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRole(new ManagedRole("ADMIN", "Another admin", Role.ADMIN)));
    }

    @Test
    void rejectsInvalidRole() {
        assertThrows(IllegalArgumentException.class, () -> new ManagedRole("", "Test", Role.USER));
        assertThrows(IllegalArgumentException.class, () -> new ManagedRole("GOOD", "", Role.USER));
    }

    @Test
    void editsRoleName() {
        service.createRole(new ManagedRole("HELPER", "Helper", Role.USER));
        assertEquals("Customer Helper", service.updateRole("HELPER",
                new ManagedRole("HELPER", "Customer Helper", Role.USER)).name());
    }

    @Test
    void refusesCodeChange() {
        service.createRole(new ManagedRole("HELPER", "Helper", Role.USER));
        assertThrows(IllegalArgumentException.class, () -> service.updateRole("HELPER",
                new ManagedRole("DIFFERENT", "Helper", Role.USER)));
    }

    @Test
    void deletesUnusedRole() {
        service.createRole(new ManagedRole("HELPER", "Helper", Role.USER));
        service.deleteRole("HELPER");
        assertTrue(service.getRoles().isEmpty());
    }

    @Test
    void assignmentUpdatesUserSystemRole() {
        service.createRole(new ManagedRole("SUPPORT", "Support", Role.STAFF));
        var assigned = service.assignRole(1L, "SUPPORT");
        assertEquals("SUPPORT", assigned.roleCode());
        assertEquals(Role.STAFF, users.get(1L).getRole());
        assertEquals(1, service.getAssignments().size());
    }

    @Test
    void cannotDeleteAssignedRoleOrChangeItsBaseRole() {
        service.createRole(new ManagedRole("SUPPORT", "Support", Role.STAFF));
        service.assignRole(1L, "SUPPORT");
        assertThrows(IllegalArgumentException.class, () -> service.deleteRole("SUPPORT"));
        assertThrows(IllegalArgumentException.class, () -> service.updateRole("SUPPORT",
                new ManagedRole("SUPPORT", "Support", Role.ADMIN)));
    }

    @Test
    void unassignmentKeepsUsersBaseRole() {
        service.createRole(new ManagedRole("SUPPORT", "Support", Role.STAFF));
        service.assignRole(1L, "SUPPORT");
        service.unassignRole(1L);
        assertTrue(service.getAssignments().isEmpty());
        assertEquals(Role.STAFF, users.get(1L).getRole());
    }

    @Test
    void rejectsMissingUser() {
        service.createRole(new ManagedRole("SUPPORT", "Support", Role.STAFF));
        assertThrows(NoSuchElementException.class, () -> service.assignRole(99L, "SUPPORT"));
    }

    @Test
    void cleansAssignmentWhenLegacyRoleIsChanged() {
        service.createRole(new ManagedRole("SUPPORT", "Support", Role.STAFF));
        service.assignRole(1L, "SUPPORT");
        users.get(1L).setRole(Role.ADMIN);
        assertTrue(service.getAssignments().isEmpty());
    }
}
