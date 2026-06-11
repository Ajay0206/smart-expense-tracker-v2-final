/* ═══════════════════════════════════════════════════════════
   ExpensePro v2  —  API & Utilities
   ═══════════════════════════════════════════════════════════ */

const BASE = 'http://localhost:8080/api';

// ── Auth helpers ─────────────────────────────────────────
function getToken()  { return localStorage.getItem('ep_token'); }
function getUser()   { try { return JSON.parse(localStorage.getItem('ep_user')); } catch { return null; } }
function saveSession(data) {
  localStorage.setItem('ep_token', data.token);
  localStorage.setItem('ep_user', JSON.stringify(data));
}
function clearSession() {
  localStorage.removeItem('ep_token');
  localStorage.removeItem('ep_user');
}
function requireAuth() {
  if (!getToken()) { window.location.href = 'index.html'; return false; }
  return true;
}
function logout() { clearSession(); window.location.href = 'index.html'; }

// ── Fetch wrapper ─────────────────────────────────────────
async function api(path, opts = {}) {
  const token = getToken();
  const isBlob = opts.blob;
  delete opts.blob;

  const res = await fetch(BASE + path, {
    ...opts,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(opts.headers || {})
    }
  });

  if (res.status === 401) { logout(); return null; }

  if (isBlob) return res.blob();

  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || data.message || 'Request failed');
  return data;
}

// ── Named API calls ───────────────────────────────────────
const API = {
  register:  d       => api('/auth/register', { method:'POST', body: JSON.stringify(d) }),
  login:     d       => api('/auth/login',    { method:'POST', body: JSON.stringify(d) }),

  dashboard: ()      => api('/expenses/dashboard'),
  categories:()      => api('/expenses/categories'),

  expenses:  p       => api('/expenses?' + new URLSearchParams(
                           Object.fromEntries(Object.entries(p).filter(([,v])=>v!=null&&v!=='')))),
  addExp:    d       => api('/expenses',     { method:'POST', body: JSON.stringify(d) }),
  editExp:   (id,d)  => api(`/expenses/${id}`,{ method:'PUT',  body: JSON.stringify(d) }),
  delExp:    id      => api(`/expenses/${id}`,{ method:'DELETE'}),

  exportPdf: (y,m)   => api(`/reports/pdf?year=${y}&month=${m}`, { blob: true }),
  updateProfile: d   => api('/expenses/profile', { method:'PATCH', body: JSON.stringify(d) }),
};

// ── Formatting ────────────────────────────────────────────
const SYMBOLS = { INR:'₹', USD:'$', EUR:'€', GBP:'£', JPY:'¥', AUD:'A$' };

function fmtCur(val, currency) {
  const sym = SYMBOLS[currency] || (currency + ' ');
  return sym + parseFloat(val || 0).toLocaleString('en-IN', { minimumFractionDigits:2, maximumFractionDigits:2 });
}

function fmtDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' });
}

function fmtShortDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('en-IN', { day:'2-digit', month:'short' });
}

function initials(name = '') {
  return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
}

// ── Toast ─────────────────────────────────────────────────
function toast(msg, type = 'ok') {
  const el = document.getElementById('toast');
  if (!el) return;
  el.textContent = (type === 'ok' ? '✓ ' : '✕ ') + msg;
  el.className = `show toast-${type}`;
  setTimeout(() => el.className = '', 3200);
}

// ── Sidebar init ──────────────────────────────────────────
function initSidebar() {
  const user = getUser();
  if (!user) return;
  const avatar = document.getElementById('sidebarAvatar');
  const name   = document.getElementById('sidebarName');
  const email  = document.getElementById('sidebarEmail');
  if (avatar) { avatar.textContent = initials(user.fullName); avatar.style.background = user.avatarColor || '#6366f1'; }
  if (name)   name.textContent = user.fullName || user.username;
  if (email)  email.textContent = user.email || '';
}

// ── Chart defaults ────────────────────────────────────────
function chartDefaults() {
  Chart.defaults.color          = '#94a3b8';
  Chart.defaults.borderColor    = '#1e1e33';
  Chart.defaults.font.family    = 'Inter, sans-serif';
  Chart.defaults.font.size      = 11;
}

// ── Category emoji map ────────────────────────────────────
const CAT_EMOJI = {
  'Food & Dining':'🍽️','Transportation':'🚗','Shopping':'🛍️',
  'Entertainment':'🎬','Health':'💊','Bills & Utilities':'⚡',
  'Education':'📚','Travel':'✈️','Groceries':'🛒',
  'Investment':'📈','Rent & Housing':'🏠','Miscellaneous':'📦'
};
function catEmoji(name) { return CAT_EMOJI[name] || '💸'; }
