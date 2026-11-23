import { useCallback, useEffect, useState } from "react";

const blank = { code: "", name: "", baseRole: "USER" };
const systemRoles = ["USER", "STAFF", "ADMIN", "SUPER_ADMIN"];

/** Sends a request to the role management API. */
async function roleApi(path = "", options = {}) {
    const response = await fetch(`/api/roles${path}`, {
        headers: { "Content-Type": "application/json" },
        ...options
    });
    const text = await response.text();
    let body = null;
    try { body = text ? JSON.parse(text) : null; }
    catch { body = { error: text }; }
    if (!response.ok) throw new Error(body?.error || `HTTP ${response.status}`);
    return body;
}

/** Dashboard for custom roles and existing user assignments. */
export default function RoleManagementPage() {
    const [roles, setRoles] = useState([]);
    const [users, setUsers] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [form, setForm] = useState(blank);
    const [editCode, setEditCode] = useState(null);
    const [selectedUser, setSelectedUser] = useState("");
    const [selectedRole, setSelectedRole] = useState("");
    const [message, setMessage] = useState("");
    const [busy, setBusy] = useState(false);

    const refresh = useCallback(async () => {
        const [newRoles, newAssignments, response] = await Promise.all([
            roleApi(), roleApi("/assignments"), fetch("/api/users")
        ]);
        if (!response.ok) throw new Error(`Unable to load users: HTTP ${response.status}`);
        setRoles(newRoles);
        setAssignments(newAssignments);
        setUsers(await response.json());
    }, []);

    useEffect(() => {
        refresh().catch(error => setMessage(error.message));
    }, [refresh]);

    async function perform(operation, successMessage) {
        setBusy(true);
        setMessage("");
        try {
            await operation();
            await refresh();
            setMessage(successMessage);
        } catch (error) {
            setMessage(error.message);
        } finally {
            setBusy(false);
        }
    }

    /** Creates or edits a custom role. */
    async function saveRole(event) {
        event.preventDefault();
        const payload = { ...form, code: form.code.trim().toUpperCase(), name: form.name.trim() };
        await perform(async () => {
            await roleApi(editCode ? `/${encodeURIComponent(editCode)}` : "", {
                method: editCode ? "PUT" : "POST",
                body: JSON.stringify(payload)
            });
            setForm(blank);
            setEditCode(null);
        }, editCode ? "Role updated." : "Role created.");
    }

    function beginEdit(role) {
        setForm(role);
        setEditCode(role.code);
        setMessage("");
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    async function deleteRole(code) {
        if (!window.confirm(`Delete custom role ${code}?`)) return;
        await perform(() => roleApi(`/${encodeURIComponent(code)}`, { method: "DELETE" }), "Role deleted.");
    }

    async function assign(event) {
        event.preventDefault();
        if (!selectedUser || !selectedRole) return;
        await perform(() => roleApi(`/assignments/${selectedUser}`, {
            method: "PUT", body: JSON.stringify({ roleCode: selectedRole })
        }), "Role assigned to user.");
    }

    async function unassign(userId) {
        if (!window.confirm(`Remove custom role from user ${userId}?`)) return;
        await perform(() => roleApi(`/assignments/${userId}`, { method: "DELETE" }), "Custom role removed.");
    }

    return <main className="container role-page">
        <header><h1>Role Management</h1><p>Create, edit, delete and assign custom roles — User Story 1.1</p></header>
        <section className="panel">
            <h2>{editCode ? `Edit Role ${editCode}` : "Create Custom Role"}</h2>
            <form onSubmit={saveRole}>
                <label>Role Code
                    <input required maxLength={50} placeholder="SUPPORT_AGENT" value={form.code}
                        disabled={!!editCode || busy} onChange={e => setForm({ ...form, code: e.target.value })}/>
                </label>
                <label>Display Name
                    <input required maxLength={100} placeholder="Support Agent" value={form.name}
                        onChange={e => setForm({ ...form, name: e.target.value })}/>
                </label>
                <label>Base System Role
                    <select value={form.baseRole} onChange={e => setForm({ ...form, baseRole: e.target.value })}>
                        {systemRoles.map(role => <option key={role} value={role}>{role}</option>)}
                    </select>
                </label>
                <div className="actions">
                    <button type="submit" disabled={busy}>{editCode ? "Save Changes" : "Add Role"}</button>
                    {editCode && <button type="button" className="secondary" onClick={() => { setForm(blank); setEditCode(null); }}>Cancel</button>}
                </div>
            </form>
            <p className="role-help">Custom roles inherit their base role permissions. Built-in roles cannot be deleted.</p>
        </section>
        {message && <p role="status" className="message">{message}</p>}
        <section className="panel">
            <h2>Custom Roles</h2>
            <div className="role-table-wrap"><table><thead><tr><th>Code</th><th>Name</th><th>Base Role</th><th>Actions</th></tr></thead><tbody>
                {roles.map(role => <tr key={role.code}>
                    <td>{role.code}</td><td>{role.name}</td><td>{role.baseRole}</td>
                    <td><button className="secondary" type="button" disabled={busy} onClick={() => beginEdit(role)}>Edit</button>{" "}
                        <button className="danger" type="button" disabled={busy} onClick={() => deleteRole(role.code)}>Delete</button></td>
                </tr>)}
            </tbody></table></div>
            {roles.length === 0 && <p>No custom roles yet.</p>}
        </section>
        <section className="panel">
            <h2>Assign Custom Role to User</h2>
            <form onSubmit={assign}>
                <label>User
                    <select value={selectedUser} required onChange={e => setSelectedUser(e.target.value)}>
                        <option value="">Select user</option>
                        {users.map(user => <option key={user.id} value={user.id}>{user.id} — {user.name} ({user.role})</option>)}
                    </select>
                </label>
                <label>Custom Role
                    <select value={selectedRole} required onChange={e => setSelectedRole(e.target.value)}>
                        <option value="">Select role</option>
                        {roles.map(role => <option key={role.code} value={role.code}>{role.name} — {role.baseRole}</option>)}
                    </select>
                </label>
                <div className="actions"><button type="submit" disabled={busy || !roles.length}>Assign Role</button></div>
            </form>
            <h2>Current Assignments</h2>
            <div className="role-table-wrap"><table><thead><tr><th>User</th><th>Custom Role</th><th>Actions</th></tr></thead><tbody>
                {assignments.map(item => <tr key={item.userId}>
                    <td>{item.userId} — {users.find(user => user.id === item.userId)?.name || "Unknown"}</td>
                    <td>{item.roleCode}</td>
                    <td><button type="button" className="danger" disabled={busy} onClick={() => unassign(item.userId)}>Remove</button></td>
                </tr>)}
            </tbody></table></div>
            {assignments.length === 0 && <p>No custom role assignments.</p>}
        </section>
    </main>;
}
