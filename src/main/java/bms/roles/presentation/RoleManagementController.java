package bms.roles.presentation;

import bms.roles.application.RoleManagementService;
import bms.roles.domain.ManagedRole;
import bms.roles.domain.RoleAssignment;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** REST API for custom roles and user role assignments. */
@RestController
@RequestMapping("/api/roles")
public class RoleManagementController {
    private final RoleManagementService service;

    public RoleManagementController(RoleManagementService service) {
        this.service = service;
    }

    @GetMapping
    public List<ManagedRole> getRoles() {
        return service.getRoles();
    }

    @GetMapping("/{code}")
    public ManagedRole getRole(@PathVariable String code) {
        return service.getRole(code);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ManagedRole createRole(@RequestBody ManagedRole role) {
        return service.createRole(role);
    }

    @PutMapping("/{code}")
    public ManagedRole updateRole(@PathVariable String code, @RequestBody ManagedRole role) {
        return service.updateRole(code, role);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable String code) {
        service.deleteRole(code);
    }

    @GetMapping("/assignments")
    public List<RoleAssignment> getAssignments() {
        return service.getAssignments();
    }

    /** Assigns a custom role to a user and updates the legacy system role. */
    @PutMapping("/assignments/{userId}")
    public RoleAssignment assignRole(@PathVariable Long userId, @RequestBody AssignmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Role assignment cannot be empty.");
        }
        return service.assignRole(userId, request.roleCode());
    }

    @DeleteMapping("/assignments/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassignRole(@PathVariable Long userId) {
        service.unassignRole(userId);
    }

    public record AssignmentRequest(String roleCode) { }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException ex) {
        return Map.of("error", ex.getMessage());
    }
}
