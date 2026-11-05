import { useEffect, useState } from 'react';
import { getTickets, openTicket, resolveTicket, closeTicket } from './api.js';
import TicketForm from './components/TicketForm.jsx';
import TicketTable from './components/TicketTable.jsx';

const TABS = ['ALL', 'OPEN', 'RESOLVED', 'CLOSED'];

export default function App() {
  const [tickets, setTickets] = useState([]);
  const [tab, setTab] = useState('ALL');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  // Load the tickets once after the first render.
  useEffect(() => { run(getTickets, false); }, []);

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

  const shown = tab === 'ALL' ? tickets : tickets.filter(t => t.status === tab);

  return <div className="app-shell">
    <header className="masthead">
      <div>
        <p className="eyebrow">CT5106 / BUSINESS MANAGEMENT SYSTEM</p>
        <h1>Service Portal</h1>
        <p className="subtitle">Open repair tickets, follow their progress and close them once they are fixed</p>
      </div>
      <div className="label-demo">BMS repair portal</div>
    </header>

    {error && <p className="error" role="alert">{error}</p>}

    <div className="two-columns portal-layout">
      <TicketForm busy={busy} onOpen={description => run(() => openTicket(description))} />

      <section className="panel">
        <div className="toolbar">
          <h2>Tickets</h2>
          <button className="secondary" onClick={() => run(getTickets, false)} disabled={busy}>Refresh</button>
        </div>
        <nav className="tabs">
          {TABS.map(name => <button key={name} className={tab === name ? 'tab active' : 'tab'} onClick={() => setTab(name)}>
            {name.charAt(0) + name.slice(1).toLowerCase()}
          </button>)}
        </nav>
        <TicketTable tickets={shown} busy={busy}
          onResolve={id => run(() => resolveTicket(id))}
          onClose={id => run(() => closeTicket(id))} />
      </section>
    </div>

    <p className="footer-note">Tickets are saved in PostgreSQL through the Spring Boot API, so they are still here after a restart.</p>
  </div>;
}
