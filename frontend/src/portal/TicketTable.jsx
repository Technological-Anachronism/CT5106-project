// Shows tickets and the action that makes sense for each status.
// OPEN tickets wait for staff. RESOLVED ones can be closed. Resolve is not on this page.
export default function TicketTable({ tickets, onClose, busy }) {
  if (tickets.length === 0) return <p className="supporting">No tickets here.</p>;

  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Ticket</th>
            <th>Description</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {tickets.map(ticket => (
            <tr key={ticket.id}>
              <td>{ticket.id}</td>
              <td>{ticket.description}</td>
              <td>
                <span className={`status ${ticket.status.toLowerCase()}`}>{ticket.status}</span>
              </td>
              <td>
                {ticket.status === 'OPEN' && (
                  <span className="done">Waiting for staff</span>
                )}
                {ticket.status === 'RESOLVED' && (
                  <button className="small" disabled={busy} onClick={() => onClose(ticket.id)}>
                    Close
                  </button>
                )}
                {ticket.status === 'CLOSED' && <span className="done">Done</span>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
