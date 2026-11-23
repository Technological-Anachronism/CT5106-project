package bms.roles.domain;

/** Links an existing user ID to a custom role code. */
public record RoleAssignment(Long userId, String roleCode) { }
