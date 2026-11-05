// Shows a list of tickets with the action allowed for each status.
export default function TicketTable({ tickets, onResolve, onClose, busy }) {
  if (tickets.length === 0) return <p className="supporting">No tickets here.</p>;

  return <div className="table-scroll"><table>
    <thead><tr><th>Ticket</th><th>Description</th><th>Status</th><th>Action</th></tr></thead>
    <tbody>{tickets.map(ticket => <tr key={ticket.id}>
      <td>{ticket.id}</td>
      <td>{ticket.description}</td>
      <td><span className={`status ${ticket.status.toLowerCase()}`}>{ticket.status}</span></td>
      <td>
        {ticket.status === 'OPEN' && <button className="small" disabled={busy} onClick={() => onResolve(ticket.id)}>Resolve</button>}
        {ticket.status === 'RESOLVED' && <button className="small" disabled={busy} onClick={() => onClose(ticket.id)}>Close</button>}
        {ticket.status === 'CLOSED' && <span className="done">Done</span>}
      </td>
    </tr>)}</tbody>
  </table></div>;
}
