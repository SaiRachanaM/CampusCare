/* User Dashboard Controller */
let myComplaints = [];
let availableLocations = [];
let availableAssets = [];

document.addEventListener('DOMContentLoaded', async () => {
  await initUserDashboard();
});

async function initUserDashboard() {
  const user = await loadUserProfile();
  if (!user) return;

  if (user.role === 'MAINTENANCE_STAFF') {
    window.location.href = '/staff-dashboard.html';
    return;
  }

  // Set user welcome UI
  document.getElementById('user-name-display').textContent = user.name;

  // Load locations for problem report form
  await loadLocations();

  // Load user complaints
  await refreshComplaints();

  // Search & Filter listeners
  const searchInput = document.getElementById('search-complaint');
  const statusFilter = document.getElementById('filter-status');

  if (searchInput) searchInput.addEventListener('input', applyFilters);
  if (statusFilter) statusFilter.addEventListener('change', applyFilters);

  // Report problem form listener
  const reportForm = document.getElementById('report-problem-form');
  if (reportForm) {
    reportForm.addEventListener('submit', handleComplaintSubmit);
  }

  // Location change listener for cascade asset loading
  const locSelect = document.getElementById('report-location');
  if (locSelect) {
    locSelect.addEventListener('change', (e) => loadAssetsForLocation(e.target.value));
  }
}

async function loadUserProfile() {
  try {
    const res = await ApiClient.get('/auth/me');
    if (res.success) {
      return res.data;
    }
  } catch (err) {
    window.location.href = '/index.html';
  }
  return null;
}

async function loadLocations() {
  try {
    const res = await ApiClient.get('/locations');
    if (res.success) {
      availableLocations = res.data;
      const select = document.getElementById('report-location');
      if (select) {
        select.innerHTML = '<option value="">-- Select Campus Location --</option>' +
          availableLocations.map(l => `<option value="${l.locationId}">${l.building} - ${l.floor}, ${l.room}</option>`).join('');
      }
    }
  } catch (err) {
    showToast('Failed to load campus locations', 'error');
  }
}

async function loadAssetsForLocation(locationId) {
  const assetSelect = document.getElementById('report-asset');
  if (!assetSelect) return;

  if (!locationId) {
    assetSelect.innerHTML = '<option value="">-- Optional: No Specific Asset --</option>';
    assetSelect.disabled = true;
    return;
  }

  try {
    const res = await ApiClient.get(`/assets?locationId=${locationId}`);
    if (res.success) {
      availableAssets = res.data;
      assetSelect.disabled = false;
      assetSelect.innerHTML = '<option value="">-- Optional: No Specific Asset --</option>' +
        availableAssets.map(a => `<option value="${a.assetId}">${a.assetName} (${a.assetType}) [${a.status}]</option>`).join('');
    }
  } catch (err) {
    assetSelect.innerHTML = '<option value="">-- No assets in location --</option>';
    assetSelect.disabled = true;
  }
}

async function refreshComplaints() {
  try {
    const res = await ApiClient.get('/complaints/my');
    if (res.success) {
      myComplaints = res.data;
      updateMetricCards(myComplaints);
      renderComplaintTable(myComplaints);
    }
  } catch (err) {
    showToast('Failed to fetch your complaints', 'error');
  }
}

function updateMetricCards(list) {
  const total = list.length;
  const pending = list.filter(c => c.status === 'REPORTED' || c.status === 'ASSIGNED').length;
  const inProgress = list.filter(c => c.status === 'IN_PROGRESS').length;
  const closed = list.filter(c => c.status === 'COMPLETED' || c.status === 'CLOSED').length;

  document.getElementById('metric-total').textContent = total;
  document.getElementById('metric-pending').textContent = pending;
  document.getElementById('metric-inprogress').textContent = inProgress;
  document.getElementById('metric-closed').textContent = closed;
}

function renderComplaintTable(list) {
  const tbody = document.getElementById('complaints-tbody');
  if (!tbody) return;

  if (list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; padding: 2rem; color: var(--text-muted);">No maintenance complaints found. Click "Report a Problem" to log a new ticket.</td></tr>`;
    return;
  }

  tbody.innerHTML = list.map(c => `
    <tr>
      <td><strong>#${c.complaintId}</strong></td>
      <td>
        <div style="font-weight: 600;">${escapeHtml(c.title)}</div>
        <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(c.issue.substring(0, 60))}${c.issue.length > 60 ? '...' : ''}</div>
      </td>
      <td>${c.location ? `${escapeHtml(c.location.building)} (${escapeHtml(c.location.room)})` : 'N/A'}</td>
      <td><span class="badge prio-${c.priority.toLowerCase()}">${c.priority}</span></td>
      <td><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
      <td>
        <button class="btn-secondary" onclick="viewComplaintDetail(${c.complaintId})">View Timeline</button>
      </td>
    </tr>
  `).join('');
}

function applyFilters() {
  const query = document.getElementById('search-complaint').value.toLowerCase();
  const status = document.getElementById('filter-status').value;

  const filtered = myComplaints.filter(c => {
    const matchesSearch = c.title.toLowerCase().includes(query) ||
                          c.issue.toLowerCase().includes(query) ||
                          (c.location && c.location.building.toLowerCase().includes(query));
    const matchesStatus = !status || c.status === status;
    return matchesSearch && matchesStatus;
  });

  renderComplaintTable(filtered);
}

async function handleComplaintSubmit(e) {
  e.preventDefault();
  const locationId = document.getElementById('report-location').value;
  const assetIdVal = document.getElementById('report-asset').value;
  const title = document.getElementById('report-title').value.trim();
  const priority = document.getElementById('report-priority').value;
  const issue = document.getElementById('report-issue').value.trim();
  const submitBtn = e.target.querySelector('button[type="submit"]');

  if (!locationId || !title || !issue || !priority) {
    showToast('Please complete all required fields', 'error');
    return;
  }

  const payload = {
    locationId: parseInt(locationId),
    assetId: assetIdVal ? parseInt(assetIdVal) : null,
    title,
    issue,
    priority
  };

  try {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Submitting...';

    const res = await ApiClient.post('/complaints', payload);
    if (res.success) {
      showToast('Maintenance problem reported successfully!', 'success');
      closeModal('report-modal');
      e.target.reset();
      await refreshComplaints();
    }
  } catch (err) {
    showToast(err.message || 'Failed to submit complaint', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Submit Complaint';
  }
}

async function viewComplaintDetail(id) {
  try {
    const res = await ApiClient.get(`/complaints/${id}`);
    if (res.success) {
      const data = res.data;
      const c = data.complaint;

      document.getElementById('detail-id').textContent = `#${c.complaintId}`;
      document.getElementById('detail-title').textContent = c.title;
      document.getElementById('detail-issue').textContent = c.issue;
      document.getElementById('detail-location').textContent = `${c.location.building}, ${c.location.floor}, ${c.location.room}`;
      document.getElementById('detail-asset').textContent = c.asset ? `${c.asset.assetName} (${c.asset.assetType})` : 'None Specified';
      document.getElementById('detail-status').className = `badge badge-${c.status.toLowerCase()}`;
      document.getElementById('detail-status').textContent = c.status;
      document.getElementById('detail-priority').className = `badge prio-${c.priority.toLowerCase()}`;
      document.getElementById('detail-priority').textContent = c.priority;
      document.getElementById('detail-assigned').textContent = c.assignedTo ? c.assignedTo.name : 'Unassigned';
      document.getElementById('detail-due').textContent = c.dueAt ? new Date(c.dueAt).toLocaleString() : 'N/A';

      // Repair details if available
      const repairBox = document.getElementById('detail-repair-box');
      if (data.repair) {
        repairBox.style.display = 'block';
        document.getElementById('repair-desc').textContent = data.repair.description;
        document.getElementById('repair-cost').textContent = `$${parseFloat(data.repair.cost).toFixed(2)}`;
        document.getElementById('repair-tech').textContent = data.repair.recordedBy ? data.repair.recordedBy.name : 'Staff';
        document.getElementById('repair-date').textContent = new Date(data.repair.repairDate).toLocaleString();
      } else {
        repairBox.style.display = 'none';
      }

      // Render timeline
      const timelineContainer = document.getElementById('detail-timeline');
      timelineContainer.innerHTML = data.history.map(h => `
        <div class="timeline-item">
          <div class="timeline-header">
            <span class="badge badge-${h.status.toLowerCase()}">${h.status}</span>
            <span class="timeline-time">${new Date(h.changedAt).toLocaleString()}</span>
          </div>
          <div class="timeline-remarks">${escapeHtml(h.remarks || 'Status update')}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.1rem;">By: ${h.changedBy ? escapeHtml(h.changedBy.name) : 'System'}</div>
        </div>
      `).join('');

      openModal('detail-modal');
    }
  } catch (err) {
    showToast('Failed to load complaint details', 'error');
  }
}

function openModal(id) {
  document.getElementById(id).classList.add('active');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('active');
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}
