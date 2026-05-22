/* ═══════════════════════════════════════════════════════════
   ExpensePro v2  —  Shared Sidebar Renderer
   ═══════════════════════════════════════════════════════════ */

function renderSidebar(activePage) {
  const nav = [
    { href:'dashboard.html', icon:'📊', label:'Dashboard' },
    { href:'expenses.html',  icon:'💳', label:'Expenses'  },
    { href:'reports.html',   icon:'📈', label:'Reports'   },
    { href:'settings.html',  icon:'⚙️',  label:'Settings'  },
  ];
  const links = nav.map(n => `
    <a href="${n.href}" class="nav-link ${n.href === activePage ? 'active' : ''}">
      <span class="icon">${n.icon}</span> ${n.label}
    </a>`).join('');

  return `
  <div class="sidebar-brand">
    <div class="brand-icon">💸</div>
    <div>
      <div class="brand-name">ExpensePro</div>
      <div class="brand-ver">v2.0</div>
    </div>
  </div>

  <div class="sidebar-user">
    <div class="user-avatar" id="sidebarAvatar">EP</div>
    <div>
      <div class="user-name"  id="sidebarName">Loading...</div>
      <div class="user-email" id="sidebarEmail"></div>
    </div>
  </div>

  <nav class="sidebar-nav">
    <div class="nav-section">Navigation</div>
    ${links}
  </nav>

  <div class="sidebar-footer">
    <button class="btn-logout" onclick="logout()">🚪 Sign Out</button>
  </div>`;
}
