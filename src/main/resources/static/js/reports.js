/* Reports & Analytics Controller */
document.addEventListener('DOMContentLoaded', async () => {
  await initReportsPage();
});

async function initReportsPage() {
  const user = await loadUserProfile();
  if (!user || user.role !== 'MAINTENANCE_STAFF') {
    window.location.href = '/user-dashboard.html';
    return;
  }

  document.getElementById('staff-name-display').textContent = user.name;

  // Load database-backed report API endpoints
  await Promise.all([
    loadRepairCostsReport(),
    loadStatusBreakdownReport(),
    loadPriorityBreakdownReport(),
    loadOverdueReport(),
    loadFailingAssetsReport()
  ]);
}

async function loadUserProfile() {
  try {
    const res = await ApiClient.get('/auth/me');
    if (res.success) return res.data;
  } catch (err) {
    window.location.href = '/index.html';
  }
  return null;
}

async function loadRepairCostsReport() {
  try {
    const res = await ApiClient.get('/reports/repair-costs');
    if (res.success && res.data) {
      const d = res.data;
      document.getElementById('report-total-cost').textContent = `$${parseFloat(d.totalCost || 0).toFixed(2)}`;
      document.getElementById('report-avg-cost').textContent = `$${parseFloat(d.averageCost || 0).toFixed(2)}`;
      document.getElementById('report-total-repairs').textContent = d.totalRepairs || 0;
    }
  } catch (err) {
    showToast('Failed to load repair cost analytics', 'error');
  }
}

async function loadStatusBreakdownReport() {
  try {
    const res = await ApiClient.get('/reports/counts-by-status');
    if (res.success && res.data) {
      const container = document.getElementById('chart-status-bars');
      const data = res.data;
      const max = Math.max(...data.map(d => d.count), 1);

      container.innerHTML = data.map(d => {
        const pct = Math.round((d.count / max) * 100);
        return `
          <div class="bar-row">
            <div class="bar-label-group">
              <span>${d.status}</span>
              <span>${d.count} tickets</span>
            </div>
            <div class="bar-track">
              <div class="bar-fill" style="width: ${pct}%;"></div>
            </div>
          </div>
        `;
      }).join('');
    }
  } catch (err) {
    document.getElementById('chart-status-bars').innerHTML = '<div style="color:var(--text-muted);">Failed to load status breakdown</div>';
  }
}

async function loadPriorityBreakdownReport() {
  try {
    const res = await ApiClient.get('/reports/counts-by-priority');
    if (res.success && res.data) {
      const container = document.getElementById('chart-priority-bars');
      const data = res.data;
      const max = Math.max(...data.map(d => d.count), 1);

      container.innerHTML = data.map(d => {
        const pct = Math.round((d.count / max) * 100);
        return `
          <div class="bar-row">
            <div class="bar-label-group">
              <span>${d.priority}</span>
              <span>${d.count} tickets</span>
            </div>
            <div class="bar-track">
              <div class="bar-fill" style="width: ${pct}%; background: linear-gradient(90deg, #f97316, #ef4444);"></div>
            </div>
          </div>
        `;
      }).join('');
    }
  } catch (err) {
    document.getElementById('chart-priority-bars').innerHTML = '<div style="color:var(--text-muted);">Failed to load priority breakdown</div>';
  }
}

async function loadOverdueReport() {
  try {
    const res = await ApiClient.get('/reports/overdue');
    if (res.success && res.data) {
      const tbody = document.getElementById('overdue-tbody');
      const list = res.data;

      if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding: 1.5rem; color: var(--text-muted);">No overdue complaints! SLA compliance is 100%.</td></tr>`;
        return;
      }

      tbody.innerHTML = list.map(item => {
        const c = item.complaint;
        return `
          <tr>
            <td><strong>#${c.complaintId}</strong></td>
            <td>${escapeHtml(c.title)}</td>
            <td><span class="badge prio-${c.priority.toLowerCase()}">${c.priority}</span></td>
            <td><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
            <td style="color: #ef4444; font-weight: 700;">+${item.hoursOverdue} hrs</td>
          </tr>
        `;
      }).join('');
    }
  } catch (err) {
    showToast('Failed to load overdue report', 'error');
  }
}

async function loadFailingAssetsReport() {
  try {
    const res = await ApiClient.get('/reports/frequently-failing-assets');
    if (res.success && res.data) {
      const tbody = document.getElementById('failing-assets-tbody');
      const list = res.data;

      if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; padding: 1.5rem; color: var(--text-muted);">No asset failures reported.</td></tr>`;
        return;
      }

      tbody.innerHTML = list.map(item => `
        <tr>
          <td><strong>#${item.assetId}</strong></td>
          <td>${escapeHtml(item.assetName)}</td>
          <td>${escapeHtml(item.building)}</td>
          <td><span class="badge prio-urgent">${item.failureCount} failures</span></td>
        </tr>
      `).join('');
    }
  } catch (err) {
    showToast('Failed to load asset failure report', 'error');
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
