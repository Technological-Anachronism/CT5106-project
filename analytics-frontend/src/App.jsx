import { useEffect, useRef, useState } from 'react';

function basicAuth(user, pass) {
  const bytes = new TextEncoder().encode(`${user}:${pass}`);
  return 'Basic ' + btoa(Array.from(bytes, b => String.fromCharCode(b)).join(''));
}

async function getOverview(credentials, signal) {
  const response = await fetch('/api/analytics/overview', {
    headers: { Authorization: basicAuth(credentials.username, credentials.password) },
    cache: 'no-store',
    credentials: 'omit',
    signal
  });
  if (response.status === 401) throw new Error('Incorrect administrator credentials.');
  if (response.status === 503) throw new Error('Configure the Analytics admin environment variables in Spring Boot.');
  if (!response.ok) throw new Error(`Analytics API returned HTTP ${response.status}.`);
  return response.json();
}

export default function App() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [session, setSession] = useState(null);
  const [report, setReport] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const activeRequest = useRef(null);
  useEffect(() => () => activeRequest.current?.abort(), []);

  // Use the same request when logging in or clicking Refresh.
  async function refresh(credentials) {
    activeRequest.current?.abort();
    const request = new AbortController();
    activeRequest.current = request;
    setLoading(true);
    setError('');
    try {
      const next = await getOverview(credentials, request.signal);
      if (activeRequest.current !== request || request.signal.aborted) return;
      setReport(next);
      setSession(credentials); // Memory only: nothing stored in cookies or localStorage.
      setUsername('');
      setPassword('');
    } catch (e) {
      if (activeRequest.current !== request || request.signal.aborted) return;
      setError(e.message);
      setReport(null);
      if (e.message.includes('credentials')) setSession(null);
    } finally {
      if (activeRequest.current === request) {
        activeRequest.current = null;
        setLoading(false);
      }
    }
  }

  // Clear the dashboard and the credentials kept in memory.
  function signOut() {
    activeRequest.current?.abort();
    activeRequest.current = null;
    setLoading(false);
    setSession(null);
    setReport(null);
    setUsername('');
    setPassword('');
    setError('');
  }

  return <div className="app-shell">
    <header className="masthead">
      <div>
        <p className="eyebrow">CT5106 / BUSINESS MANAGEMENT SYSTEM</p>
        <h1>Service Analytics</h1>
        <p className="subtitle">Service status, outstanding tasks and repair-ticket feature usage</p>
      </div>
      <span className="label-demo">ASSIGNMENT 2 PROTOTYPE</span>
    </header>

    {!session && <section className="panel login">
      <h2>Analytics administrator</h2>
      <p className="supporting">Use the temporary credentials configured when starting Spring Boot. This is not yet connected to the team's user roles.</p>
      <form onSubmit={e => { e.preventDefault(); refresh({username, password}); }}>
        <label>Username<input required autoComplete="username" value={username} onChange={e => setUsername(e.target.value)} /></label>
        <label>Password<input required type="password" autoComplete="current-password" value={password} onChange={e => setPassword(e.target.value)} /></label>
        <button disabled={loading || !username || !password}>{loading ? 'Loading…' : 'View dashboard'}</button>
      </form>
    </section>}

    {session && <div className="toolbar">
      <p>Read-only analytics based on the team's live ticket repository.</p>
      <div><button onClick={() => refresh(session)} disabled={loading}>Refresh</button>
      <button className="secondary" onClick={signOut}>Sign out</button></div>
    </div>}
    {error && <p className="error" role="alert">{error}</p>}

    {report && <main>
      <div className="summary-grid">
        {[
          ['Total tickets', report.tickets.total],
          ['Open tickets', report.tickets.open],
          ['Resolved', report.tickets.resolved],
          ['Closed', report.tickets.closed]
        ].map(([name, value]) => <section className="panel stat" key={name}>
          <span>{name}</span><strong>{value}</strong>
        </section>)}
      </div>
      <div className="two-columns">
        <section className="panel">
          <h2>Service status</h2>
          <p className="supporting">Reads real tickets via TicketRepository.</p>
          <div className="table-scroll"><table>
            <thead><tr><th>Service</th><th>Status</th><th>Outstanding tasks</th></tr></thead>
            <tbody>{report.services.map(service => <tr key={service.name}>
              <td>{service.name}</td><td><span className="status active">{service.status}</span></td>
              <td>{service.outstandingTasks}</td>
            </tr>)}</tbody>
          </table></div>
        </section>
        <section className="panel">
          <h2>Feature usage</h2>
          <p className="supporting">Estimates derived from ticket states, not historical events.</p>
          {report.services.flatMap(service => service.features.map(feature =>
            <div className="bar-row" key={`${service.name}/${feature.name}`}>
              <div className="bar-label"><span>{feature.name}</span><b>{feature.usageCount}</b></div>
              <div className="bar-track"><div className="bar-fill" style={{width: `${100 * feature.usageCount / Math.max(1, report.tickets.total)}%`}} /></div>
            </div>
          ))}
        </section>
      </div>
      <section className="panel">
        <h2>Service feature tasks</h2>
        <div className="table-scroll"><table>
          <thead><tr><th>Service</th><th>Feature</th><th>Outstanding</th><th>Estimated usage</th></tr></thead>
          <tbody>{report.services.flatMap(service => service.features.map(feature =>
            <tr key={`${service.name}/${feature.name}`}><td>{service.name}</td><td>{feature.name}</td>
              <td>{feature.outstandingTasks}</td><td>{feature.usageCount}</td></tr>))}</tbody>
        </table></div>
      </section>
      <p className="footer-note">Data generated: {new Date(report.generatedAt).toLocaleString()}. {report.limitation}</p>
    </main>}
  </div>;
}
