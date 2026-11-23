
package bms.domain;

/**
 * Stores basic user information.
 * Includes user ID, name, email, and role.
 */
public class User {

    private Long id;

    private String name;

    private String email;

    private Role role;

    public User() {
    }

    /**
     * Creates a user with the given information.
     * @param id The user's ID
     * @param name The user's name
     * @param email The user's email address
     * @param role The user's role
     */
    public User(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    /**
     * Gets the user's ID.
     * @return The user's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user's ID.
     * @param id The user's ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user's name.
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user's name.
     * @param name The user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user's email address.
     * @return The user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email The user's email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's role.
     * @return The user's role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     * @param role The user's role
     */
    public void setRole(Role role) {
        this.role = role;
    }
}
