const fs = require('fs');
const assert = require('assert/strict');
// Tests the async logout state transition; no DOM/browser is emulated.
const root = require('path').resolve(__dirname, '../..');
const esbuild = require(root + '/node_modules/esbuild');
const source = fs.readFileSync(root + '/src/analytics/AnalyticsPage.jsx', 'utf8');
const code = esbuild.transformSync(source, { loader: 'jsx', format: 'cjs', jsx: 'automatic' }).code;
const states = [];
let cursor = 0;
const ref = { current: null };
const react = {
  useState(initial) {
    const slot = cursor++;
    if (!(slot in states)) states[slot] = initial;
    return [states[slot], value => { states[slot] = value; }];
  },
  useRef: () => ref,
  useEffect: () => {}
};
const jsx = (type, props) => ({ type, props });
const moduleObject = { exports: {} };
new Function('require', 'module', 'exports', code)(name => {
  if (name === 'react') return react;
  if (name === 'react/jsx-runtime') return { jsx, jsxs: jsx };
  throw new Error('Unexpected import: ' + name);
}, moduleObject, moduleObject.exports);
const render = () => { cursor = 0; return moduleObject.exports.default(); };
function elements(node, type) {
  if (!node || typeof node !== 'object') return [];
  if (Array.isArray(node)) return node.flatMap(n => elements(n, type));
  return [...(node.type === type ? [node] : []), ...elements(node.props?.children, type)];
}
const report = { tickets: { total: 0, open: 0, resolved: 0, closed: 0 }, services: [], generatedAt: new Date().toISOString() };
let pending;
let signal;
global.fetch = (_url, options) => {
  signal = options.signal;
  // Intentionally ignore abort: prove the stale-response guard also works.
  return new Promise(resolve => { pending = () => resolve({ status: 200, ok: true, json: async () => report }); });
};
const flush = () => new Promise(resolve => setImmediate(resolve));
(async () => {
  let tree = render();
  const inputs = elements(tree, 'input');
  inputs[0].props.onChange({ target: { value: 'test-user' } });
  inputs[1].props.onChange({ target: { value: 'test-password' } });
  tree = render();
  elements(tree, 'form')[0].props.onSubmit({ preventDefault() {} });
  pending();
  await flush();
  tree = render();
  elements(tree, 'button').find(b => b.props.children === 'Refresh').props.onClick();
  elements(tree, 'button').find(b => b.props.children === 'Sign out').props.onClick();
  assert.equal(signal.aborted, true);
  pending();
  await flush();
  tree = render();
  assert.equal(elements(tree, 'form').length, 1);
  assert.equal(elements(tree, 'main').length, 0);
  assert.equal(states[2], null);
  assert.equal(states[3], null);
  assert.equal(states[5], false);
  console.log('PASS: late refresh response cannot restore session or dashboard after sign out.');
})().catch(error => { console.error(error); process.exitCode = 1; });
