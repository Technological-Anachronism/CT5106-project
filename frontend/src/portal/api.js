// All calls to the ticket API in one place.
const BASE = '/api/portal/tickets';

async function send(url, method = 'GET') {
  const response = await fetch(url, { method, cache: 'no-store' });
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.error || `Portal API returned HTTP ${response.status}.`);
  return body;
}

export const getTickets = () => send(BASE);
export const openTicket = description =>
  send(`${BASE}?description=${encodeURIComponent(description)}`, 'POST');
// Close only. Resolve is on Service Management so the assignee check stays in one place.
export const closeTicket = id => send(`${BASE}/${id}/close`, 'POST');
