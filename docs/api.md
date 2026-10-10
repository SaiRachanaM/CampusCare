# Campus Maintenance & Accountability System — REST API Documentation

## Overview & Security Model

The Campus Maintenance API exposes RESTful JSON endpoints. Authentication is **Stateless Bearer JWT Authentication**.
Protected endpoints require passing the issued JWT token in the `Authorization` request header:

```http
Authorization: Bearer <your_jwt_token_here>
```

---

## Standard ApiResponse Wrapper Schema

All API responses follow a standardized JSON envelope structure:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-10-10T00:00:00"
}
```

In error scenarios (`400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict`), `success` is `false` and `message` details the failure condition.

---

## 1. System Health Endpoint

### `GET /api/health`
- **Access Level:** Public
- **Description:** Returns server health and operational status.
- **Successful Response (200 OK):**
  ```json
  {
    "status": "UP",
    "service": "Campus Maintenance & Accountability System",
    "phase": "Phase 1 - Database Foundations & Entities Complete"
  }
  ```

---

## 2. Authentication Endpoints

### `POST /api/auth/register`
- **Access Level:** Public
- **Description:** Registers a new campus user with `USER` role.
- **Request Body (`RegisterRequest`):**
  ```json
  {
    "name": "John Student",
    "email": "john.student@campus.edu",
    "password": "Password@123"
  }
  ```
- **Successful Response (201 Created):**
  ```json
  {
    "success": true,
    "message": "User registered successfully",
    "data": {
      "id": 1,
      "name": "John Student",
      "email": "john.student@campus.edu",
      "role": "USER",
      "createdAt": "2026-10-10T00:00:00"
    }
  }
  ```
- **Error Responses:**
  - `400 Bad Request`: Validation errors (e.g. invalid email format, blank fields).
  - `409 Conflict`: Email address already registered in system.

### `POST /api/auth/login`
- **Access Level:** Public
- **Description:** Authenticates user credentials and issues a JWT token.
- **Request Body (`LoginRequest`):**
  ```json
  {
    "email": "john.student@campus.edu",
    "password": "Password@123"
  }
  ```
- **Successful Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Authentication successful",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "tokenType": "Bearer",
      "expiresInMs": 86400000,
      "user": {
        "id": 1,
        "name": "John Student",
        "email": "john.student@campus.edu",
        "role": "USER"
      }
    }
  }
  ```
- **Error Response:** `401 Unauthorized` (Invalid email or password).

### `GET /api/auth/me`
- **Access Level:** Authenticated (`USER` or `MAINTENANCE_STAFF`)
- **Description:** Returns profile details of the currently authenticated user.
- **Successful Response (200 OK):** User DTO.

---

## 3. Location Endpoints

### `GET /api/locations`
- **Access Level:** Authenticated (`USER` or `MAINTENANCE_STAFF`)
- **Description:** Lists all registered campus buildings, floors, and rooms.
- **Successful Response (200 OK):** List of Location DTOs.

### `POST /api/locations`
- **Access Level:** `MAINTENANCE_STAFF` Only
- **Description:** Creates a new campus location record.
- **Request Body (`LocationRequest`):**
  ```json
  {
    "building": "Engineering Building",
    "floor": "Ground Floor",
    "room": "Auditorium"
  }
  ```
- **Successful Response (201 Created):** Created Location DTO.
- **Error Response:** `409 Conflict` (Location with building, floor, and room combination already exists).

---

## 4. Asset Endpoints

### `GET /api/assets`
- **Access Level:** Authenticated (`USER` or `MAINTENANCE_STAFF`)
- **Query Parameters:** `locationId` (Optional filter)
- **Description:** Retrieves campus assets, optionally filtered by location.
- **Successful Response (200 OK):** List of Asset DTOs.

### `POST /api/assets`
- **Access Level:** `MAINTENANCE_STAFF` Only
- **Description:** Registers a new asset at a specified location.
- **Request Body (`AssetRequest`):**
  ```json
  {
    "assetName": "Lab Projector Sony HD",
    "assetType": "Electronics",
    "locationId": 1,
    "status": "FUNCTIONAL"
  }
  ```
- **Successful Response (201 Created):** Created Asset DTO.

---

## 5. Complaint Endpoints

### `POST /api/complaints`
- **Access Level:** Authenticated (`USER` or `MAINTENANCE_STAFF`)
- **Description:** Submits a new maintenance ticket. Sets initial status to `REPORTED` and calculates SLA due date based on priority.
- **Request Body (`CreateComplaintRequest`):**
  ```json
  {
    "locationId": 1,
    "assetId": 1,
    "title": "Projector Display Flicker",
    "issue": "Screen blinks red every 10 seconds during lectures.",
    "priority": "HIGH"
  }
  ```
- **Successful Response (201 Created):** Complaint Response DTO.

### `GET /api/complaints/my`
- **Access Level:** Authenticated (`USER` or `MAINTENANCE_STAFF`)
- **Description:** Lists complaints submitted by the logged-in user.
- **Successful Response (200 OK):** List of Complaint Response DTOs.

### `GET /api/complaints`
- **Access Level:** `MAINTENANCE_STAFF` Only
- **Query Parameters:** `status` (Optional), `priority` (Optional)
- **Description:** Retrieves all campus-wide complaints with optional status and priority filters.
- **Successful Response (200 OK):** List of Complaint Response DTOs.

### `GET /api/complaints/{id}`
- **Access Level:** Complaint Reporter or `MAINTENANCE_STAFF`
- **Description:** Fetches full details of a ticket including location, asset, reported user, assigned staff, repair record (if any), and status history timeline.
- **Successful Response (200 OK):** Detailed Complaint DTO.

### `PUT /api/complaints/{id}/assign`
- **Access Level:** `MAINTENANCE_STAFF` Only
- **Description:** Assigns ticket to a maintenance technician and transitions status to `ASSIGNED`.
- **Request Body (`AssignComplaintRequest`):**
  ```json
  {
    "assignedToStaffId": 3,
    "remarks": "Assigned technician Bob for electrical inspection."
  }
  ```
- **Successful Response (200 OK):** Updated Complaint DTO.

### `PUT /api/complaints/{id}/status`
- **Access Level:** `MAINTENANCE_STAFF` Only
- **Description:** Updates ticket status. Transitions to `COMPLETED` or `CLOSED` strictly require a recorded repair log.
- **Request Body (`UpdateStatusRequest`):**
  ```json
  {
    "status": "IN_PROGRESS",
    "remarks": "Replaced faulty power supply capacitor."
  }
  ```
- **Successful Response (200 OK):** Updated Complaint DTO.
- **Error Response:** `400 Bad Request` (Invalid status transition or missing repair record prior to completion).

---

## 6. Repair Endpoints

### `POST /api/repairs`
- **Access Level:** `MAINTENANCE_STAFF` Only
- **Description:** Logs a formal repair record for a complaint ticket. Enforces 1:1 relationship per complaint.
- **Request Body (`RecordRepairRequest`):**
  ```json
  {
    "complaintId": 1,
    "repairDate": "2026-10-10T10:30:00",
    "description": "Replaced faulty lamp module and re-calibrated lens alignment.",
    "cost": 45.50
  }
  ```
- **Successful Response (201 Created):** Created Repair DTO.
- **Error Response:** `400 Bad Request` / `409 Conflict` (Repair already recorded for this complaint ID).

---

## 7. Report Endpoints (`MAINTENANCE_STAFF` Only)

- `GET /api/reports/pending`: Returns list of active non-terminal tickets (`REPORTED`, `ASSIGNED`, `IN_PROGRESS`).
- `GET /api/reports/overdue`: Returns list of unresolved complaints past their SLA `due_at` timestamp.
- `GET /api/reports/frequently-failing-assets`: Returns asset breakdown statistics sorted by complaint frequency.
- `GET /api/reports/counts-by-status`: Returns status counts (e.g. `{"REPORTED": 2, "IN_PROGRESS": 1, ...}`).
- `GET /api/reports/counts-by-priority`: Returns priority counts (e.g. `{"HIGH": 3, "LOW": 1, ...}`).
- `GET /api/reports/repair-costs`: Returns total repair expenditure, average cost, and total repair count.
