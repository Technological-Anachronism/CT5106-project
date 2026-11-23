import { useState } from 'react';

// Form for opening a ticket. The description stays in state until submit works.
export default function TicketForm({ onOpen, busy }) {
  const [description, setDescription] = useState('');

  async function submit(e) {
    e.preventDefault();
    const ok = await onOpen(description);
    if (ok) setDescription('');
  }

  return (
    <section className="panel">
      <h2>Open a ticket</h2>
      <p className="supporting">Describe the problem and where it is. New tickets start as OPEN.</p>
      <form className="ticket-form" onSubmit={submit}>
        <label>
          Description
          <input
            value={description}
            placeholder="e.g. Router not working in CSB ground floor"
            onChange={e => setDescription(e.target.value)}
          />
        </label>
        <button disabled={busy || !description.trim()}>Open ticket</button>
      </form>
    </section>
  );
}
