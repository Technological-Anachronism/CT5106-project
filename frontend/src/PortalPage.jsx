import { useEffect, useState } from 'react';
import { getTickets, openTicket, closeTicket } from './portal/api.js';
import TicketForm from './portal/TicketForm.jsx';
import TicketTable from './portal/TicketTable.jsx';

const FILTERS = ['ALL', 'OPEN', 'RESOLVED', 'CLOSED'];

// Service Portal tab. Open a ticket, check on it, close it once staff have fixed it.
// No resolve button here so people cannot skip the assignee check.
export default function PortalPage() {
  const [tickets, setTickets] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  // Load the tickets once after the first render.
  useEffect(() => {
    run(getTickets, false);
  }, []);

  // Runs an API call, then reloads the list so the view matches the database.
  async function run(action, reload = true) {
    setBusy(true);
    setError('');
    try {
      const result = await action();
      setTickets(reload ? await getTickets() : result);
      return true;
    } catch (e) {
      setError(e.message);
      return false;
    } finally {
      setBusy(false);
    }
  }

  const shown = filter === 'ALL' ? tickets : tickets.filter(t => t.status === filter);

  return (
    <div className="portal-page">
      <header className="masthead">
        <div>
          <p className="eyebrow">CT5106 / BUSINESS MANAGEMENT SYSTEM</p>
          <h1>Service Portal</h1>
          <p className="subtitle">
            Open repair tickets, follow their progress and close them once they are fixed
          </p>
        </div>
        <div className="label-demo">BMS repair portal</div>
      </header>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      <div className="two-columns portal-layout">
        <TicketForm busy={busy} onOpen={description => run(() => openTicket(description))} />

        <section className="panel">
          <div className="toolbar">
            <h2>Tickets</h2>
            <button className="secondary" onClick={() => run(getTickets, false)} disabled={busy}>
              Refresh
            </button>
          </div>
          <nav className="filter-tabs">
            {FILTERS.map(name => (
              <button
                key={name}
                className={filter === name ? 'filter-tab active' : 'filter-tab'}
                onClick={() => setFilter(name)}
              >
                {name.charAt(0) + name.slice(1).toLowerCase()}
              </button>
            ))}
          </nav>
          <TicketTable
            tickets={shown}
            busy={busy}
            onClose={id => run(() => closeTicket(id))}
          />
        </section>
      </div>

      <p className="footer-note">
        Tickets are saved in PostgreSQL through the Spring Boot API, so they are still here after a
        restart. Staff resolve tickets on the Service Management tab.
      </p>
    </div>
  );
}
