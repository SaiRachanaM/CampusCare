# Campus Maintenance & Accountability System

A DBMS mini-project for managing campus maintenance requests, asset tracking, technician assignment, repair logging, status accountability, and operational reports. Built with Java 21, Spring Boot 3.2.5, Spring Data JPA, Spring Security, JWT authentication, MySQL 8.0, and a responsive HTML5/CSS3/Vanilla JavaScript web interface.

---

## 1. Project Overview & Objectives

### Problem Statement
Educational institutions often struggle with inefficient maintenance reporting, untracked repair costs, delayed ticket resolution, lack of staff accountability, and missing historical audit logs for facilities and assets.

### System Objectives
- **Centralized Facility Management:** Streamline reporting of physical asset and location defects across campus buildings.
- **Accountability & Auditing:** Maintain an immutable status history tracking every status change, timestamps, and staff actions.
- **Enforced Repair Validation:** Require a formal repair record (including description, technician ID, and repair cost) before any complaint can be completed or closed.
- **Role-Based Access Control (RBAC):** Provide distinct access levels for standard campus users (`USER`) and maintenance administrators/technicians (`MAINTENANCE_STAFF`).
- **Data Insights:** Offer comprehensive real-time dashboards for pending tickets, SLA overdue complaints, repair expenditures, and frequently failing assets.

---

## 2. Main Features

- **Stateless JWT Authentication:** Secure login and registration with BCrypt password hashing and role authorization headers (`Bearer <token>`).
- **User Dashboard (`/user-dashboard.html`):** Normal users can lodge maintenance complaints, select campus locations and assets, and monitor personal ticket progress.
- **Staff Control Center (`/staff-dashboard.html`):** Staff can inspect all campus tickets, assign staff members, transition complaint statuses (`REPORTED` → `ASSIGNED` → `IN_PROGRESS` → `COMPLETED` → `CLOSED`), and record detailed repairs.
- **Reports Dashboard (`/reports.html`):** Metrics on active complaints, SLA overdue items, equipment breakdown rates, status/priority breakdown, and total repair expenses.
- **Database Consistency & Integrity:** Standardized MySQL 8.0 relational schema enforcing 1:1 repair constraints, foreign key restrictions, and unique location indexes.

---

## 3. Technology Stack

- **Backend Framework:** Java 21, Spring Boot 3.2.5
- **Security:** Spring Security 6, JWT (JSON Web Tokens), BCrypt Password Encoder
- **Database & ORM:** MySQL 8.0+, Spring Data JPA, Hibernate 6
- **Testing Frameworks:** JUnit 5, Spring Security Test, H2 In-Memory Database (for unit/integration tests)
- **Frontend Layer:** Semantic HTML5, Custom CSS3 Design System, Modern Vanilla JavaScript (ES6 fetch API, DOM manipulation)
- **Build & Dependency Management:** Apache Maven, Maven Wrapper (`mvnw.cmd` / `mvnw`)

---

## 4. User Roles & Permissions

| Feature / Action | `USER` | `MAINTENANCE_STAFF` |
| :--- | :---: | :---: |
| Register Account / Log In | Yes | Yes |
| View Profile (`/api/auth/me`) | Yes | Yes |
| Submit Complaint | Yes | Yes |
| View Personal Complaints (`/api/complaints/my`) | Yes | Yes |
| View Complaint Details & History | Own Complaints Only | All Campus Complaints |
| View All Campus Complaints (`/api/complaints`) | No (403 Forbidden) | Yes |
| Manage Locations & Assets | View Only | Create & View |
| Assign Technician (`PUT /api/complaints/{id}/assign`) | No (403 Forbidden) | Yes |
| Update Status (`PUT /api/complaints/{id}/status`) | No (403 Forbidden) | Yes |
| Record Repair (`POST /api/repairs`) | No (403 Forbidden) | Yes |
| Access Reports Dashboard (`/api/reports/*`) | No (403 Forbidden) | Yes |

---

## 5. System Architecture

The project adopts a layered MVC / REST architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    Web Frontend Interface                   │
│   (index.html, user-dashboard.html, staff-dashboard.html)    │
└──────────────────────────────┬──────────────────────────────┘
                               │ REST API Calls (Bearer JWT)
┌──────────────────────────────▼──────────────────────────────┐
│                    Spring Boot Security Filter               │
│     (JwtAuthenticationFilter & SecurityConfig - RBAC)       │
└──────────────────────────────┬──────────────────────────────┘
                               │ Controller Layer
┌──────────────────────────────▼──────────────────────────────┐
│                    Spring Boot Controllers                  │
│  (AuthController, ComplaintController, RepairController, etc.)│
└──────────────────────────────┬──────────────────────────────┘
                               │ Service Layer & DTOs
┌──────────────────────────────▼──────────────────────────────┐
│                    Domain Services & Logic                  │
│ (ComplaintService, RepairService, StatusHistoryService, etc.)│
└──────────────────────────────┬──────────────────────────────┘
                               │ Spring Data JPA Repositories
┌──────────────────────────────▼──────────────────────────────┐
│                    MySQL 8.0 Database                       │
│    (app_user, location, asset, complaint, repair, history)  │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Database Tables Overview

The system relies on 6 relational database tables defined in `database/schema.sql`:

1. **`app_user`**: Stores user credentials, names, emails, roles (`USER` or `MAINTENANCE_STAFF`), and creation timestamps.
2. **`location`**: Campus locations cataloged by `building`, `floor`, and `room` with a composite unique constraint.
3. **`asset`**: Physical assets attached to locations (`asset_name`, `asset_type`, `status`).
4. **`complaint`**: Maintenance tickets reported by users with priority (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), status (`REPORTED`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CLOSED`), location, asset, reporter, and assigned technician.
5. **`repair`**: Repair log detailing date, description, cost, and technician ID. Enforces a 1:1 relationship with `complaint` via `UNIQUE(complaint_id)`.
6. **`status_history`**: Audit trail storing every status transition, timestamp, remark, and staff user ID.

---

## 7. Prerequisites

- **Java Development Kit (JDK):** Java 21 JDK installed and set in `JAVA_HOME`.
- **Database:** MySQL Server 8.0 or higher.
- **Build Tool:** Maven Wrapper (`mvnw.cmd` on Windows, `./mvnw` on Linux/macOS) included in the repository.
- **Terminal:** PowerShell or Command Prompt on Windows.

---

## 8. Database Setup

### Step 1: Create Database
In MySQL Workbench or MySQL Command Line:
```sql
CREATE DATABASE IF NOT EXISTS campus_maintenance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Import Schema & Seed Data
Execute the schema script `database/schema.sql`.

**Option A: MySQL Command Line (PowerShell)**
```powershell
Get-Content database/schema.sql | mysql -u root -p campus_maintenance_db
```

**Option B: MySQL Workbench**
1. Open MySQL Workbench and connect to your local MySQL instance.
2. Open `database/schema.sql` (File → Open SQL Script).
3. Select `campus_maintenance_db` and click **Execute** (lightning bolt icon).

---

## 9. Environment Variable Configuration

The application reads configuration from environment variables with local defaults set in `src/main/resources/application.properties`.

Set the environment variables in your PowerShell session before running the application:

```powershell
# Set Database Credentials
$env:DB_URL="jdbc:mysql://localhost:3306/campus_maintenance_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YourMySQLPasswordHere"

# Securely Generate & Set a Local Base64 JWT Secret
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()

# Optional Server Port
$env:SERVER_PORT="8080"
```

> **Note:** The `JWT_SECRET` must be set in the exact same PowerShell session used to launch the application. Never commit real secrets to version control.

---

## 10. Building & Running Tests

Run the automated test suite (uses H2 in-memory database, so local MySQL state remains unaffected):

```powershell
.\mvnw.cmd test
```

### Actual Test Results Obtained
- **Total Executed Tests:** 42
- **Passed:** 42
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0
- **Build Result:** `BUILD SUCCESS`

---

## 11. Application Startup & Access

To launch the application server:

```powershell
.\mvnw.cmd spring-boot:run
```

Once started:
- **Web Application Frontend:** [http://localhost:8080](http://localhost:8080)
- **API Documentation:** [`docs/api.md`](file:///c:/Users/sai_r/OneDrive/Desktop/CampusMaintenance/docs/api.md)
- **Database Design:** [`docs/database-design.md`](file:///c:/Users/sai_r/OneDrive/Desktop/CampusMaintenance/docs/database-design.md)
- **Demonstration Guide:** [`docs/demo-guide.md`](file:///c:/Users/sai_r/OneDrive/Desktop/CampusMaintenance/docs/demo-guide.md)
- **Testing Documentation:** [`docs/testing.md`](file:///c:/Users/sai_r/OneDrive/Desktop/CampusMaintenance/docs/testing.md)
- **Workflow State Machine:** [`docs/workflow.md`](file:///c:/Users/sai_r/OneDrive/Desktop/CampusMaintenance/docs/workflow.md)

---

## 12. Pre-seeded Demo Accounts

The `database/schema.sql` populates four demo accounts (default password for all seed accounts is `Password@123`):

| Role | Name | Email | Password |
| :--- | :--- | :--- | :--- |
| `USER` | John Student | `john.student@campus.edu` | `hellooooooo` |
| `USER` | Alice Faculty | `alice.faculty@campus.edu` | `hellooooooo` |
| `MAINTENANCE_STAFF` | Bob Technician | `bob.tech@campus.edu` | `hellooooooo` |
| `MAINTENANCE_STAFF` | Sarah Supervisor | `sarah.staff@campus.edu` | `hellooooooo` |

---

## 13. Known Limitations

- **File Attachments:** Image/file attachment uploads for complaints are not implemented in the current scope; descriptions are stored as text fields.
- **Email Notifications:** Email/SMS alerts on ticket assignment are simulated in log statements rather than connected to an external SMTP server.
- **Single-Location Scope:** Locations are managed per building/floor/room without external mapping GIS coordinates.
