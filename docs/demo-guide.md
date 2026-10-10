# Project Evaluation Demonstration Guide

This guide provides a step-by-step walkthrough for presenting the **Campus Maintenance & Accountability System** during an academic project evaluation or lab viva.

---

## 1. Pre-Demo Setup & Environment Preparation

### Step 1.1: Verify MySQL Service & Database
Ensure your MySQL server is running on `localhost:3306`. Import the schema script to reset/initialize the database tables and seed data:

```powershell
Get-Content database/schema.sql | mysql -u root -p campus_maintenance_db
```

**Expected Result:** All 6 tables (`app_user`, `location`, `asset`, `complaint`, `repair`, `status_history`) are created and populated with 4 seed users, 4 locations, 4 assets, 2 initial complaints, and 3 status history records.

### Step 1.2: Set Environment Variables in PowerShell
Open a PowerShell terminal and set the configuration variables:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/campus_maintenance_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YourMySQLPasswordHere"

# Generate local JWT Secret
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

**Expected Result:** PowerShell environment variables are initialized for the active shell context.

### Step 1.3: Start Spring Boot Application
In the same PowerShell session, launch the backend application:

```powershell
.\mvnw.cmd spring-boot:run
```

**Expected Result:** Tomcat server starts on port `8080` with message `Started CampusMaintenanceApplication in X.XXX seconds`.

### Step 1.4: Launch the Frontend
Open Google Chrome or Microsoft Edge and navigate to:
`http://localhost:8080`

**Expected Result:** The application homepage loads displaying system summary, quick action cards, and navigation links.

---

## 2. Seed Demo Accounts Quick Reference

| Role | Name | Email | Password | Purpose in Demo |
| :--- | :--- | :--- | :--- | :--- |
| `USER` | John Student | `john.student@campus.edu` | `Password@123` | Demonstrates student ticket submission |
| `USER` | Alice Faculty | `alice.faculty@campus.edu` | `Password@123` | Demonstrates faculty ticket tracking |
| `MAINTENANCE_STAFF` | Bob Technician | `bob.tech@campus.edu` | `Password@123` | Demonstrates assigned technician work & repair logging |
| `MAINTENANCE_STAFF` | Sarah Supervisor | `sarah.staff@campus.edu` | `Password@123` | Demonstrates ticket assignment & supervisor management |

---

## 3. Step-by-Step Evaluator Demonstration Script

### Step 1: User Registration & Authentication Flow
1. Navigate to **Login / Register** page (`http://localhost:8080/index.html`).
2. Click **Register User Account**, enter details (e.g., Name: `Dave Student`, Email: `dave.student@campus.edu`, Password: `Password@123`), and click **Register**.
3. Log in with the newly registered user credentials or seed account `john.student@campus.edu`.

> **Evaluator Script:**
> *"Here we demonstrate our secure authentication subsystem. Passwords are hashed using BCrypt before persistence in MySQL. Upon successful authentication, the backend issues a stateless JWT containing the user's role claim (`USER` or `MAINTENANCE_STAFF`), which is stored securely in `sessionStorage` and sent as a `Bearer` token header with every REST API invocation."*

---

### Step 2: Normal User Complaint Submission (`USER` Role)
1. After logging in as `john.student@campus.edu`, you are automatically redirected to the **User Dashboard** (`/user-dashboard.html`).
2. In the **Report Maintenance Complaint** form:
   - Select Location: `Science Block - 2nd Floor - Lab 204`
   - Select Asset: `AC Split Unit 2.0 Ton`
   - Enter Title: `AC Water Leakage in Lab 204`
   - Enter Issue Description: `Water is dripping heavily onto desk 3 from the AC indoor unit.`
   - Priority: `HIGH`
3. Click **Submit Complaint Ticket**.

> **Evaluator Script:**
> *"A normal user can submit maintenance tickets bound to specific campus locations and assets. When submitted, the backend automatically sets the initial status to `REPORTED`, records `reported_by` as the authenticated user's ID, and calculates an SLA due date based on the selected priority. Notice that the UI instantly updates the personal complaint list."*

---

### Step 3: Staff Management & Technician Assignment (`MAINTENANCE_STAFF` Role)
1. Click **Logout**, then log in as staff member `sarah.staff@campus.edu` (`Password@123`).
2. Redirect to **Staff Control Center** (`/staff-dashboard.html`).
3. Observe all campus complaints in the table (including tickets created by all users).
4. Locate the ticket `AC Water Leakage in Lab 204` (Status: `REPORTED`).
5. Click **Assign Ticket**:
   - Select Staff Member: `Bob Technician (bob.tech@campus.edu)`
   - Remarks: `Assigned technician Bob for urgent HVAC drainage inspection.`
6. Click **Confirm Assignment**.

> **Evaluator Script:**
> *"Role-based access control enforces that only `MAINTENANCE_STAFF` can view all campus-wide complaints and assign tickets. Assigning a ticket transitions its status from `REPORTED` to `ASSIGNED`, links the technician's user ID to `assigned_to`, and creates an entry in the `status_history` audit table."*

---

### Step 4: Status Transition & Technician Work (`IN_PROGRESS`)
1. In the Staff Dashboard, locate the assigned complaint.
2. Click **Update Status**:
   - New Status: `IN_PROGRESS`
   - Remarks: `Inspected indoor unit drain pipe; cleared blockage and ordered replacement seal.`
3. Click **Save Status Change**.

> **Evaluator Script:**
> *"Technicians update ticket status as work proceeds. Our backend enforces state machine validation, so status changes must follow valid transitions (`ASSIGNED` → `IN_PROGRESS`). Remarks are saved in `status_history` with the staff member's ID and timestamp."*

---

### Step 5: Enforced Repair Logging & Completion (`COMPLETED` / `CLOSED`)
1. Try to directly change ticket status to `COMPLETED` *without* logging a repair. Observe the alert: *"Cannot complete ticket without a recorded repair log."*
2. Click **Log Repair Record** for the ticket:
   - Repair Date: Select current date/time
   - Cost ($): `35.00`
   - Repair Description: `Cleared blocked AC condensation drain line and replaced rubber gasket.`
3. Click **Save Repair Log**.
4. Now click **Update Status** → set to `COMPLETED` or `CLOSED` with remarks `Repair verified and functional.`

> **Evaluator Script:**
> *"This is a key accountability requirement in our DBMS design: a complaint cannot be closed or marked complete until a formal repair record is logged in the `repair` table. The `repair` table enforces a strict 1:1 relationship with `complaint` via a `UNIQUE(complaint_id)` constraint, tracking costs and parts for institutional audit."*

---

### Step 6: Reports & Analytics Dashboard
1. Click **Reports & Analytics** in the navbar (`/reports.html`).
2. Review the live analytics cards and tables:
   - **Active Pending Complaints:** Counts active tickets (`REPORTED`, `ASSIGNED`, `IN_PROGRESS`).
   - **SLA Overdue Tickets:** Highlights complaints past their `due_at` timestamp.
   - **Frequently Failing Assets:** Ranks assets by total reported complaints.
   - **Status & Priority Distribution:** Displays percentage breakdowns.
   - **Total Repair Expenditure:** Shows aggregated repair expenses and average repair cost.

> **Evaluator Script:**
> *"Our system provides administrative decision-makers with actionable operational metrics. Using SQL aggregation queries (GROUP BY, SUM, COUNT, JOINs), the backend generates real-time data on maintenance spending, high-risk assets requiring replacement, and SLA compliance performance."*

---

## 4. Verification Database Queries for Evaluator Presentation

During evaluation, you can open MySQL Command Line or Workbench to show the underlying database records backing your demonstration:

```sql
-- 1. View recent complaint state and assigned staff
SELECT complaint_id, title, priority, status, reported_by, assigned_to, due_at 
FROM complaint ORDER BY complaint_id DESC LIMIT 5;

-- 2. Verify 1:1 repair log record for completed complaint
SELECT r.repair_id, r.complaint_id, r.description, r.cost, u.name AS technician
FROM repair r
JOIN app_user u ON r.recorded_by = u.user_id;

-- 3. Verify complete audit trail of status changes
SELECT h.history_id, h.complaint_id, h.status, u.name AS changed_by, h.remarks, h.changed_at
FROM status_history h
JOIN app_user u ON h.changed_by = u.user_id
ORDER BY h.history_id ASC;
```
