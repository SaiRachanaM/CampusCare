/* Maintenance Staff Dashboard Controller */
let allComplaints = [];
let eligibleStaff = [];
let currentSelectedComplaintId = null;

document.addEventListener('DOMContentLoaded', async () => {
  await initStaffDashboard();
});

async function initStaffDashboard() {
  const user = await loadUserProfile();
  if (!user) return;

  if (user.role !== 'MAINTENANCE_STAFF') {
    window.location.href = '/user-dashboard.html';
    return;
  }

  document.getElementById('staff-name-display').textContent = user.name;

  // Load staff members for assignment dropdowns
  await loadEligibleStaff();

  // Load all campus complaints
  await refreshStaffComplaints();

  // Filters
  document.getElementById('staff-search').addEventListener('input', applyStaffFilters);
  document.getElementById('staff-filter-status').addEventListener('change', applyStaffFilters);
  document.getElementById('staff-filter-priority').addEventListener('change', applyStaffFilters);

  // Forms
  document.getElementById('assign-form').addEventListener('submit', handleAssignSubmit);
  document.getElementById('status-form').addEventListener('submit', handleStatusUpdateSubmit);
  document.getElementById('repair-form').addEventListener('submit', handleRepairSubmit);
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

async function loadEligibleStaff() {
  try {
    const res = await ApiClient.get('/auth/staff');
    if (res.success) {
      eligibleStaff = res.data;
      const select = document.getElementById('assign-staff-select');
      if (select) {
        select.innerHTML = '<option value="">-- Select Maintenance Staff Member --</option>' +
          eligibleStaff.map(s => `<option value="${s.id}">${s.name} (${s.email})</option>`).join('');
      }
    }
  } catch (err) {
    showToast('Failed to load maintenance staff dropdown', 'error');
  }
}

async function refreshStaffComplaints() {
  try {
    const res = await ApiClient.get('/complaints');
    if (res.success) {
      allComplaints = res.data;
      updateStaffMetrics(allComplaints);
      renderStaffTable(allComplaints);
    }
  } catch (err) {
    showToast('Failed to fetch campus complaints', 'error');
  }
}

function updateStaffMetrics(list) {
  const total = list.length;
  const pending = list.filter(c => c.status === 'REPORTED').length;
  const assigned = list.filter(c => c.status === 'ASSIGNED').length;
  const inProgress = list.filter(c => c.status === 'IN_PROGRESS').length;
  const completed = list.filter(c => c.status === 'COMPLETED' || c.status === 'CLOSED').length;

  const now = new Date();
  const overdue = list.filter(c => (c.status !== 'COMPLETED' && c.status !== 'CLOSED') && c.dueAt && new Date(c.dueAt) < now).length;

  document.getElementById('metric-staff-total').textContent = total;
  document.getElementById('metric-staff-pending').textContent = pending;
  document.getElementById('metric-staff-assigned').textContent = assigned;
  document.getElementById('metric-staff-inprogress').textContent = inProgress;
  document.getElementById('metric-staff-overdue').textContent = overdue;
}

function renderStaffTable(list) {
  const tbody = document.getElementById('staff-tbody');
  if (!tbody) return;

  if (list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem; color: var(--text-muted);">No campus complaints match the selected criteria.</td></tr>`;
    return;
  }

  tbody.innerHTML = list.map(c => `
    <tr>
      <td><strong>#${c.complaintId}</strong></td>
      <td>
        <div style="font-weight: 600;">${escapeHtml(c.title)}</div>
        <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(c.issue.substring(0, 50))}${c.issue.length > 50 ? '...' : ''}</div>
      </td>
      <td>${c.location ? `${escapeHtml(c.location.building)} (${escapeHtml(c.location.room)})` : 'N/A'}</td>
      <td><span class="badge prio-${c.priority.toLowerCase()}">${c.priority}</span></td>
      <td><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
      <td>${c.assignedTo ? escapeHtml(c.assignedTo.name) : '<span style="color:var(--text-muted); font-style:italic;">Unassigned</span>'}</td>
      <td>
        <div style="display: flex; gap: 0.35rem; flex-wrap: wrap;">
          <button class="btn-secondary" style="font-size:0.75rem; padding:0.25rem 0.5rem;" onclick="openAssignModal(${c.complaintId})">Assign</button>
          <button class="btn-secondary" style="font-size:0.75rem; padding:0.25rem 0.5rem;" onclick="openStatusModal(${c.complaintId}, '${c.status}')">Status</button>
          <button class="btn-secondary" style="font-size:0.75rem; padding:0.25rem 0.5rem;" onclick="openRepairModal(${c.complaintId})">Repair</button>
          <button class="btn-primary" style="font-size:0.75rem; padding:0.25rem 0.5rem;" onclick="viewStaffComplaintDetail(${c.complaintId})">View</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function applyStaffFilters() {
  const query = document.getElementById('staff-search').value.toLowerCase();
  const status = document.getElementById('staff-filter-status').value;
  const priority = document.getElementById('staff-filter-priority').value;

  const filtered = allComplaints.filter(c => {
    const matchesSearch = c.title.toLowerCase().includes(query) ||
                          c.issue.toLowerCase().includes(query) ||
                          (c.location && c.location.building.toLowerCase().includes(query));
    const matchesStatus = !status || c.status === status;
    const matchesPriority = !priority || c.priority === priority;
    return matchesSearch && matchesStatus && matchesPriority;
  });

  renderStaffTable(filtered);
}

function openAssignModal(id) {
  currentSelectedComplaintId = id;
  document.getElementById('assign-complaint-id-display').textContent = `#${id}`;
  openModal('assign-modal');
}

async function handleAssignSubmit(e) {
  e.preventDefault();
  const staffId = document.getElementById('assign-staff-select').value;
  const remarks = document.getElementById('assign-remarks').value.trim();

  if (!staffId) {
    showToast('Please select a staff member to assign', 'error');
    return;
  }

  try {
    const res = await ApiClient.put(`/complaints/${currentSelectedComplaintId}/assign`, {
      assignedToStaffId: parseInt(staffId),
      remarks
    });

    if (res.success) {
      showToast('Complaint assigned successfully!', 'success');
      closeModal('assign-modal');
      e.target.reset();
      await refreshStaffComplaints();
    }
  } catch (err) {
    showToast(err.message || 'Failed to assign complaint', 'error');
  }
}

function openStatusModal(id, currentStatus) {
  currentSelectedComplaintId = id;
  document.getElementById('status-complaint-id-display').textContent = `#${id}`;

  const select = document.getElementById('status-select');
  // Populate valid target status options depending on state machine
  select.innerHTML = '';
  if (currentStatus === 'REPORTED') {
    select.innerHTML = '<option value="ASSIGNED">ASSIGNED</option><option value="IN_PROGRESS">IN_PROGRESS</option>';
  } else if (currentStatus === 'ASSIGNED') {
    select.innerHTML = '<option value="IN_PROGRESS">IN_PROGRESS</option>';
  } else if (currentStatus === 'IN_PROGRESS') {
    select.innerHTML = '<option value="COMPLETED">COMPLETED (Requires Logged Repair)</option>';
  } else if (currentStatus === 'COMPLETED') {
    select.innerHTML = '<option value="CLOSED">CLOSED</option>';
  } else {
    select.innerHTML = '<option value="">Closed (Terminal State)</option>';
  }

  openModal('status-modal');
}

async function handleStatusUpdateSubmit(e) {
  e.preventDefault();
  const status = document.getElementById('status-select').value;
  const remarks = document.getElementById('status-remarks').value.trim();

  if (!status) {
    showToast('Please select a valid status', 'error');
    return;
  }

  try {
    const res = await ApiClient.put(`/complaints/${currentSelectedComplaintId}/status`, {
      status,
      remarks
    });

    if (res.success) {
      showToast(`Complaint status updated to ${status}!`, 'success');
      closeModal('status-modal');
      e.target.reset();
      await refreshStaffComplaints();
    }
  } catch (err) {
    showToast(err.message || 'Failed to update status', 'error');
  }
}

function openRepairModal(id) {
  currentSelectedComplaintId = id;
  document.getElementById('repair-complaint-id-display').textContent = `#${id}`;
  openModal('repair-modal');
}

async function handleRepairSubmit(e) {
  e.preventDefault();
  const description = document.getElementById('repair-form-desc').value.trim();
  const cost = document.getElementById('repair-form-cost').value;

  if (!description || cost === '') {
    showToast('Please enter repair description and cost', 'error');
    return;
  }

  try {
    const res = await ApiClient.post('/repairs', {
      complaintId: currentSelectedComplaintId,
      description,
      cost: parseFloat(cost)
    });

    if (res.success) {
      showToast('Repair record logged successfully!', 'success');
      closeModal('repair-modal');
      e.target.reset();
      await refreshStaffComplaints();
    }
  } catch (err) {
    showToast(err.message || 'Failed to log repair', 'error');
  }
}

async function viewStaffComplaintDetail(id) {
  try {
    const res = await ApiClient.get(`/complaints/${id}`);
    if (res.success) {
      const data = res.data;
      const c = data.complaint;

      document.getElementById('staff-detail-id').textContent = `#${c.complaintId}`;
      document.getElementById('staff-detail-title').textContent = c.title;
      document.getElementById('staff-detail-issue').textContent = c.issue;
      document.getElementById('staff-detail-reporter').textContent = `${c.reportedBy.name} (${c.reportedBy.email})`;
      document.getElementById('staff-detail-location').textContent = `${c.location.building}, ${c.location.floor}, ${c.location.room}`;
      document.getElementById('staff-detail-asset').textContent = c.asset ? `${c.asset.assetName} (${c.asset.assetType})` : 'None Specified';
      document.getElementById('staff-detail-status').className = `badge badge-${c.status.toLowerCase()}`;
      document.getElementById('staff-detail-status').textContent = c.status;
      document.getElementById('staff-detail-priority').className = `badge prio-${c.priority.toLowerCase()}`;
      document.getElementById('staff-detail-priority').textContent = c.priority;
      document.getElementById('staff-detail-assigned').textContent = c.assignedTo ? c.assignedTo.name : 'Unassigned';

      // Repair details if logged
      const repairBox = document.getElementById('staff-detail-repair-box');
      if (data.repair) {
        repairBox.style.display = 'block';
        document.getElementById('staff-repair-desc').textContent = data.repair.description;
        document.getElementById('staff-repair-cost').textContent = `$${parseFloat(data.repair.cost).toFixed(2)}`;
        document.getElementById('staff-repair-tech').textContent = data.repair.recordedBy ? data.repair.recordedBy.name : 'Staff';
      } else {
        repairBox.style.display = 'none';
      }

      // Timeline
      const timeline = document.getElementById('staff-detail-timeline');
      timeline.innerHTML = data.history.map(h => `
        <div class="timeline-item">
          <div class="timeline-header">
            <span class="badge badge-${h.status.toLowerCase()}">${h.status}</span>
            <span class="timeline-time">${new Date(h.changedAt).toLocaleString()}</span>
          </div>
          <div class="timeline-remarks">${escapeHtml(h.remarks || 'Status update')}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.1rem;">By: ${h.changedBy ? escapeHtml(h.changedBy.name) : 'System'}</div>
        </div>
      `).join('');

      openModal('staff-detail-modal');
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
