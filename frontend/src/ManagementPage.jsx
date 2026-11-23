import { useEffect, useState } from "react";

// Send a request to the backend and turn a rule error into a message.
async function call(path, method = "GET") {
    const response = await fetch(path, { method });
    const text = await response.text();
    let data = null;
    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = { error: text };
        }
    }
    if (!response.ok) {
        throw new Error(data?.error || `HTTP ${response.status}`);
    }
    return data;
}

// Service Management Dashboard: assign, prioritise and resolve outstanding tickets
export default function ManagementPage() {

    const [tickets, setTickets] = useState([]);
    const [staff, setStaff] = useState([]);
    const [priorityFilter, setPriorityFilter] = useState("");
    const [actingAs, setActingAs] = useState("");
    const [message, setMessage] = useState("");
    const [busy, setBusy] = useState(false);

    // Load the work queue and the staff list
    async function load() {
        try {
            const query = priorityFilter ? `?priority=${priorityFilter}` : "";
            const queue = await call(`/api/management/tickets${query}`);
            const users = await call("/api/users");
            setTickets(queue);
            setStaff(users.filter(user => user.role === "STAFF"));
        } catch (error) {
            setMessage(`Error loading tickets: ${error.message}`);
        }
    }

    useEffect(() => {
        load();
    }, [priorityFilter]);

    // Run one action, show the result, then reload the queue
    async function act(path, success) {
        setBusy(true);
        try {
            await call(path, "POST");
            setMessage(success);
        } catch (error) {
            setMessage(error.message);
        } finally {
            setBusy(false);
            load();
        }
    }

    // Show a name instead of an id once a ticket can't be reassigned
    function staffName(id) {
        const person = staff.find(user => user.id === id);
        return person ? person.name : "Unassigned";
    }

    return (
        <main className="container">

            <section className="panel">
                <h2>Service Management</h2>
                <p>Outstanding tickets, most urgent first. Only the assignee can resolve a ticket.</p>

                <div className="actions">
                    <label>
                        Show priority
                        <select value={priorityFilter} onChange={e => setPriorityFilter(e.target.value)}>
                            <option value="">All</option>
                            <option value="HIGH">HIGH</option>
                            <option value="MEDIUM">MEDIUM</option>
                            <option value="LOW">LOW</option>
                        </select>
                    </label>

                    {/* There is no login yet, so pick who is clicking Resolve */}
                    <label>
                        Acting as
                        <select value={actingAs} onChange={e => setActingAs(e.target.value)}>
                            <option value="">Choose a staff member</option>
                            {staff.map(user => (
                                <option key={user.id} value={user.id}>{user.name}</option>
                            ))}
                        </select>
                    </label>
                </div>
            </section>

            {message && (
                <p role="status" className="message">{message}</p>
            )}

            <section className="panel">
                <table>
                    <thead>
                        <tr>
                            <th>Ticket</th>
                            <th>Description</th>
                            <th>Status</th>
                            <th>Priority</th>
                            <th>Assignee</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {tickets.map(ticket => (
                            <tr key={ticket.id}>
                                <td>{ticket.id}</td>
                                <td>{ticket.description}</td>
                                <td>{ticket.status}</td>
                                <td>
                                    <select
                                        value={ticket.priority}
                                        disabled={busy}
                                        onChange={e => act(
                                            `/api/management/tickets/${ticket.id}/priority?priority=${e.target.value}`,
                                            `${ticket.id} is now ${e.target.value} priority.`
                                        )}
                                    >
                                        <option value="HIGH">HIGH</option>
                                        <option value="MEDIUM">MEDIUM</option>
                                        <option value="LOW">LOW</option>
                                    </select>
                                </td>
                                <td>
                                    {ticket.status === "OPEN" ? (
                                        <select
                                            value={ticket.assigneeId ?? ""}
                                            disabled={busy}
                                            onChange={e => act(
                                                `/api/management/tickets/${ticket.id}/assign?assigneeId=${e.target.value}`,
                                                `${ticket.id} assigned.`
                                            )}
                                        >
                                            <option value="" disabled>Unassigned</option>
                                            {staff.map(user => (
                                                <option key={user.id} value={user.id}>{user.name}</option>
                                            ))}
                                        </select>
                                    ) : (
                                        staffName(ticket.assigneeId)
                                    )}
                                </td>
                                <td>
                                    {ticket.status === "OPEN" && (
                                        <button
                                            disabled={busy || !actingAs}
                                            onClick={() => act(
                                                `/api/management/tickets/${ticket.id}/resolve?userId=${actingAs}`,
                                                `${ticket.id} resolved.`
                                            )}
                                        >
                                            Resolve
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
                {tickets.length === 0 && <p>No outstanding tickets.</p>}
            </section>

        </main>
    );
}