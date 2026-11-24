/**
 * All calls to the ticket API in one place.
 * Vite proxies /api to Spring Boot.
 */
const BASE = '/api/portal/tickets';

/**
 * Sends one request to the portal API.
 * @param {string} url
 * @param {string} [method='GET']
 */
async function send(url, method = 'GET') {
  const response = await fetch(url, { method, cache: 'no-store' });
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.error || `Portal API returned HTTP ${response.status}.`);
  return body;
}

/** Loads every ticket. */
export const getTickets = () => send(BASE);

/**
 * Opens a new ticket.
 * @param {string} description
 */
export const openTicket = description =>
  send(`${BASE}?description=${encodeURIComponent(description)}`, 'POST');

/**
 * Closes a resolved ticket.
 * Resolve is on Service Management so the assignee check stays in one place.
 * @param {string} id
 */
export const closeTicket = id => send(`${BASE}/${id}/close`, 'POST');
