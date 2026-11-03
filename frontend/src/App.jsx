
import { useEffect, useState } from "react";

// Default values for the user form
const blankForm = {
    id: "",
    name: "",
    email: "",
    role: "USER"
};

// Send requests to the Spring Boot backend
async function api(path = "", options = {}) {

    const response = await fetch(
        `/api/users${path}`,
        {
            headers: {
                "Content-Type": "application/json"
            },
            ...options
        }
    );

    const body = await response.text();

    let data = null;

    if (body) {
        try {
            data = JSON.parse(body);
        } catch {
            data = { error: body };
        }
    }

    if (!response.ok) {
        throw new Error(
            data?.error ||
            data?.detail ||
            data?.message ||
            `HTTP ${response.status}`
        );
    }

    return data;
}

// Main User Management Dashboard
export default function App() {

    // Store all users
    const [users, setUsers] = useState([]);

    // Store form information
    const [form, setForm] = useState(blankForm);

    // Track which user is being edited
    const [editingId, setEditingId] = useState(null);

    // Display success or error messages
    const [message, setMessage] = useState("");

    // Prevent duplicate operations
    const [busy, setBusy] = useState(false);

    // 1. Load all users from backend
    async function loadUsers() {

        try {
            const data = await api();

            setUsers(data);

        } catch (error) {
            setMessage(
                `Error loading users: ${error.message}`
            );
        }
    }

    // Load users when the page opens
    useEffect(() => {
        loadUsers();
    }, []);

    // 2. Select a user to edit
    function startEdit(user) {

        setEditingId(user.id);

        setForm({
            id: String(user.id),
            name: user.name,
            email: user.email,
            role: user.role
        });

        setMessage("");

        // Return to the editing form
        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    // Cancel editing
    function cancelEdit() {

        setEditingId(null);
        setForm(blankForm);
    }

    // 3. Create or update a user
    async function submit(event) {

        event.preventDefault();

        const userId = Number(form.id);

        // Validate user ID
        if (
            !Number.isSafeInteger(userId) ||
            userId <= 0
        ) {
            setMessage(
                "User ID must be a positive integer."
            );
            return;
        }

        setBusy(true);

        try {

            const payload = {
                id: userId,
                name: form.name.trim(),
                email: form.email.trim(),
                role: form.role
            };

            // Create a new user
            if (editingId === null) {

                await api("", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });

                cancelEdit();

                await loadUsers();

                setMessage("User created.");

            } else {

                // Update an existing user
                await api(`/${editingId}`, {
                    method: "PUT",
                    body: JSON.stringify(payload)
                });

                cancelEdit();

                await loadUsers();

                setMessage("User updated.");
            }

        } catch (error) {

            setMessage(error.message);

        } finally {

            setBusy(false);
        }
    }

    // 4. Delete a user
    async function removeUser(id) {

        const confirmed = window.confirm(
            `Delete user ${id}?`
        );

        if (!confirmed) {
            return;
        }

        setBusy(true);

        try {

            await api(`/${id}`, {
                method: "DELETE"
            });

            if (editingId === id) {
                cancelEdit();
            }

            await loadUsers();

            setMessage(
                `User ${id} deleted.`
            );

        } catch (error) {

            setMessage(error.message);

        } finally {

            setBusy(false);
        }
    }

    // 5. Display the dashboard
    return (

        <main className="container">

            {/* Page header */}
            <header>

                <h1>
                    Business Management System
                </h1>

                <p>
                    User Management Dashboard
                    — User Story 1.1
                </p>

            </header>

            {/* Create or edit user */}
            <section className="panel">

                <h2>
                    {editingId === null
                        ? "Create User"
                        : `Edit User #${editingId}`}
                </h2>

                <form onSubmit={submit}>

                    {/* User ID */}
                    <label>

                        User ID

                        <input
                            type="number"
                            min="1"
                            required
                            disabled={editingId !== null}
                            value={form.id}
                            onChange={(e) =>
                                setForm({
                                    ...form,
                                    id: e.target.value
                                })
                            }
                        />

                    </label>

                    {/* User name */}
                    <label>

                        Name

                        <input
                            type="text"
                            required
                            value={form.name}
                            onChange={(e) =>
                                setForm({
                                    ...form,
                                    name: e.target.value
                                })
                            }
                        />

                    </label>

                    {/* User email */}
                    <label>

                        Email

                        <input
                            type="email"
                            required
                            value={form.email}
                            onChange={(e) =>
                                setForm({
                                    ...form,
                                    email: e.target.value
                                })
                            }
                        />

                    </label>

                    {/* User role */}
                    <label>

                        Role

                        <select
                            value={form.role}
                            onChange={(e) =>
                                setForm({
                                    ...form,
                                    role: e.target.value
                                })
                            }
                        >

                            <option value="USER">
                                USER
                            </option>

                            <option value="ADMIN">
                                ADMIN
                            </option>

                            <option value="SUPER_ADMIN">
                                SUPER_ADMIN
                            </option>

                        </select>

                    </label>

                    {/* Form buttons */}
                    <div className="actions">

                        <button
                            type="submit"
                            disabled={busy}
                        >

                            {editingId === null
                                ? "Add User"
                                : "Save Changes"}

                        </button>

                        {editingId !== null && (

                            <button
                                type="button"
                                className="secondary"
                                onClick={cancelEdit}
                            >
                                Cancel
                            </button>

                        )}

                    </div>

                </form>

            </section>

            {/* Success or error message */}
            {message && (

                <p
                    role="status"
                    className="message"
                >
                    {message}
                </p>

            )}

            {/* User list */}
            <section className="panel">

                <h2>
                    All Users
                </h2>

                <table>

                    <thead>

                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Actions</th>
                        </tr>

                    </thead>

                    <tbody>

                        {users.map((user) => (

                            <tr key={user.id}>

                                <td>
                                    {user.id}
                                </td>

                                <td>
                                    {user.name}
                                </td>

                                <td>
                                    {user.email}
                                </td>

                                <td>
                                    {user.role}
                                </td>

                                <td>

                                    {/* Edit button */}
                                    <button
                                        type="button"
                                        className="secondary"
                                        onClick={() =>
                                            startEdit(user)
                                        }
                                    >
                                        Edit
                                    </button>

                                    {" "}

                                    {/* Delete button */}
                                    <button
                                        type="button"
                                        className="danger"
                                        disabled={busy}
                                        onClick={() =>
                                            removeUser(user.id)
                                        }
                                    >
                                        Delete
                                    </button>

                                </td>

                            </tr>

                        ))}

                    </tbody>

                </table>

                {users.length === 0 && (

                    <p>
                        No users found.
                        Add your first user above.
                    </p>

                )}

            </section>

            {/* Link to the existing portal */}
            <footer>

                <a href="http://localhost:8080/">
                    Existing Repair Ticket page
                </a>

                {" "}

                (teammate's prototype)

            </footer>

        </main>
    );
}
