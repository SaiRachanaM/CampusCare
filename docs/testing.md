# Automated & Manual Testing Documentation

This document outlines the testing strategy, test coverage breakdown, automated test execution results, and manual verification procedures for the **Campus Maintenance & Accountability System**.

---

## 1. Automated Test Suite Execution Summary

Automated tests are executed using the Maven Surefire plugin and JUnit 5 testing framework. An in-memory H2 database (`jdbc:h2:mem:campus_maintenance_testdb`) running in MySQL compatibility mode is configured in `src/test/resources/application-test.properties` so that automated tests execute in isolation without modifying or deleting local MySQL database records.

### Maven Test Execution Command
```powershell
.\mvnw.cmd test
```

### Actual Test Execution Output

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.campus.maintenance.ApiControllerSecurityTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 13.651 s
[INFO] Running com.campus.maintenance.AuthSecurityTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.112 s
[INFO] Running com.campus.maintenance.CampusMaintenanceApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.078 s
[INFO] Running com.campus.maintenance.DomainServicesTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.069 s
[INFO] Running com.campus.maintenance.EntityMappingTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.032 s
[INFO] Running com.campus.maintenance.JwtServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.123 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 2. Test Class & Coverage Breakdown

| Test Class | Focus / Category | Test Count | Result |
| :--- | :--- | :---: | :---: |
| `ApiControllerSecurityTest` | Integration / Security RBAC | 6 | Pass |
| `AuthSecurityTest` | Authentication & Security Service | 14 | Pass |
| `DomainServicesTest` | Domain Logic & State Machine | 12 | Pass |
| `EntityMappingTest` | JPA / Database Mapping | 5 | Pass |
| `JwtServiceTest` | Unit / JWT Token Verification | 4 | Pass |
| `CampusMaintenanceApplicationTests` | Integration / Context Load | 1 | Pass |
| **Total Suite** | | **42** | **BUILD SUCCESS** |

---

## 3. Detailed Test Functionality

### 3.1 Authentication & Security Coverage (`AuthSecurityTest`, `JwtServiceTest`)
- **Password Hashing:** Verifies BCrypt encoding upon user registration.
- **Registration Validation:** Validates required fields, email format, and rejects duplicate email registration with HTTP 409 Conflict.
- **Login Validation:** Confirms valid login returns JWT token and user DTO; invalid credentials return HTTP 401 Unauthorized.
- **JWT Operations:** Tests token creation, signature verification, expiration check, and email claim extraction.

### 3.2 Role-Based Access Control Coverage (`ApiControllerSecurityTest`)
- **Unauthenticated Access:** Verifies protected endpoints return HTTP 401 Unauthorized without JWT bearer token.
- **User Authorization (`USER` role):** Verifies users can access `/api/complaints/my` and `/api/auth/me`, but receive HTTP 403 Forbidden when calling `/api/reports/*`, `/api/complaints` (all complaints), or `/api/repairs`.
- **Staff Authorization (`MAINTENANCE_STAFF` role):** Verifies staff can access report endpoints, assign complaints, update status, and record repairs.

### 3.3 Complaint Lifecycle & Domain Validation (`DomainServicesTest`)
- **Complaint Submission:** Verifies automatic setting of `REPORTED` status and SLA due date calculation based on priority (`LOW` = 5 days, `MEDIUM` = 3 days, `HIGH` = 1 day, `URGENT` = 12 hours).
- **Technician Assignment:** Validates status transition from `REPORTED` to `ASSIGNED` and recording of assignment audit history.
- **Repair Requirement Guard:** Enforces that updating status to `COMPLETED` or `CLOSED` throws an exception if no repair record exists.
- **State Machine Guard:** Verifies invalid state transitions (e.g. `REPORTED` directly to `CLOSED` without intermediate states) are rejected.
- **Status History Audit:** Verifies every status modification creates an immutable entry in `status_history`.

### 3.4 Entity Mapping & Constraints (`EntityMappingTest`)
- Verifies JPA annotations, foreign key mapping, table definitions, and unique constraints (`app_user.email`, `location(building, floor, room)`, `repair.complaint_id`).

---

## 4. Distinction Between Test Levels

1. **Automated Unit Tests (`JwtServiceTest`, `EntityMappingTest`):** Isolated tests testing utility methods, token creation, and object mapping logic without web server initialization.
2. **Automated Integration & Security Tests (`DomainServicesTest`, `AuthSecurityTest`, `ApiControllerSecurityTest`):** Executed with `@SpringBootTest` and `@AutoConfigureMockMvc`, testing the Spring application context, JPA repositories, security filter chain, service layer, and controller REST contracts using an in-memory H2 database.
3. **Manual Browser UI Verification:** End-to-end user workflows tested manually via browser interface (`http://localhost:8080`) against a running local MySQL instance.

---

## 5. Manual Verification Checklist

The following manual UI verification steps should be performed before submission:

- [x] Verify browser page navigation (`index.html`, `user-dashboard.html`, `staff-dashboard.html`, `reports.html`).
- [x] Verify user registration and immediate login via UI forms.
- [x] Verify dropdown options populated dynamically from backend endpoints (`/api/locations`, `/api/assets`).
- [x] Verify modal popups for staff assignment, status updates, and repair entry.
- [x] Verify role routing (logging in as `USER` redirects to user dashboard; logging in as `MAINTENANCE_STAFF` redirects to staff dashboard).
- [x] Verify responsive layout across different browser window sizes.
