package bms.roles.domain;

import bms.domain.Role;
import java.util.Locale;

/** A custom role whose permissions follow an existing system role. */
public record ManagedRole(String code, String name, Role baseRole) {
    /** Validates and normalizes custom role information. */
    public ManagedRole {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Role code cannot be empty.");
        }
        code = code.trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z][A-Z0-9_]{1,49}")) {
            throw new IllegalArgumentException("Role code must be 2-50 characters and use letters, numbers or underscores.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be empty.");
        }
        name = name.trim();
        if (name.length() > 100) {
            throw new IllegalArgumentException("Role name cannot exceed 100 characters.");
        }
        if (baseRole == null) {
            throw new IllegalArgumentException("Base role cannot be empty.");
        }
    }
}
