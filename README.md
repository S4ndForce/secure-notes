# Secure Notes API

Production-style Spring Boot backend for secure note storage with JWT authentication,
ownership boundaries, soft deletes, token revocation, and scheduled cleanup jobs.

---

## What it does

Users can organize notes into folders, share them via links, and add tags for search.

The focus of this project was exploring clear security boundaries and correct backend design patterns
beyond basic CRUD.

---

## Core Features

### Auth
Stateless token-based authentication and authorization. Users can register, login, logout.


### Notes
Main entity within the program. Users can interact with this entity with CRUD operations.
Notes remain private to the user unless shared.
  ```
  {
  "id":  1,
  "content": "note from User A",
  "userName": "a@test.com",
  "folderId": 1,
  "createdAt": "2026-02-24T14:02:58.308056500Z",
  "updatedAt": "2026-02-24T14:02:58.308056500Z",
  "tags": []
  }

  ```

### Folders
Used to organize notes.
  ```
  {
    "id": 1,
    "name": "Folder",
    "userName": "a@test.com",
    "createdAt": "2026-02-24T14:08:48.083627Z",
    "updatedAt": "2026-02-24T14:08:48.083627Z"
  }
  
  ```

### Shared Links
A way for users to share notes. All links bypass authentication.
Each comes with customizable expiration times. Users can also
see all notes with active links.


  ```
  POST http://localhost:8081/notes/1/share 
  {
  "expiresInSeconds": 1800
  }
  
  (Shared access without auth -> expect 200 + NoteResponse)
  GET http://localhost:8081/shared/{{SHARE_TOKEN}}

  ```

### Tags
Normalized entities meant to categorize notes for fast searching.

  ```  
"tags": ["school", "work"]
  ```


### Async/Scheduled Jobs
Scheduled jobs and retryable services with failure handling.
#### SharedLinkCleanupJob: 
- Cleans up expired links every 60 seconds
```
{"ts":"2026-02-24 09:54:13,397", "level":"INFO", "logger":"JOBS", "msg":"Cleanup shared links: deleted=1", "traceId":"ca028b8f", "user":""}
```
#### SoftDeleteCleanupJob:
- Hard-deletes all soft-deleted notes 30 days or older
```
{"ts":"2026-02-24 10:01:00,102", "level":"INFO", "logger":"JOBS", "msg":"Purged soft-deleted notes: 3", "traceId":"f3a91c2d", "user":""}
```
#### EmailJobService:
- Sends a welcome email when invoked
```
{"ts":"2026-02-24 10:00:05,881", "level":"INFO", "logger":"JOBS", "msg":"Sending welcome email email=test@example.com", "traceId":"b2af5e70", "user":""}
```
---
### Testing
Integration tests that test authentication flow, ownership, shared link validation,
and soft delete logic. Tests run against a real database.

---
### Exception Handling
Custom exceptions to handle auth/ownership errors while properly routing Spring generated exceptions

- 400 - validation failures
- 401 - authentication failures
- 403 - ownership / authorization violations
- 404 - resource visibility boundaries
- 409 - conflict scenarios
---
## Design decisions

**JWT revocation via JTI blocklist**

Short expiry alone doesn't invalidate a compromised token. Every issued JWT carries a unique JTI,
which is stored in the `revoked_token` table on logout and checked on every authenticated request.

**Explicit Specs for all data retrieval**

All filtering and authorization (ownership, soft delete, folder membership, tags, search text) are expressed using
a `Specification<T>` object. Services build queries by combining them, rather than having dozens of `findBy...` repository methods.


**Soft delete with cascade semantics**

Cascade deletion and individual deletion cannot be treated the same. By using the explicit field `cascade_deleted_at`, individually
deleted notes may not be restored using a cascade restore, nor would cascade-deleted notes be restored using an 
individual restore. 
Logic is kept separate for both features.

**Idempotent async email jobs**

The welcome email job uses an `idempotent_actions` table to prevent duplicate sends across retries.
Spring Retry handles transient failures with exponential backoff; permanent failures are recorded
to `job_failures` for observability.
- `SharedLinkCleanupJob` and `SoftDeleteCleanupJob` do not contain idempotent features as the former does not produce external side effects, while the latter is naturally
idempotent (finds nothing to purge if ran twice).

**MDC propagation across async threads**

The thread pool task executor wraps every runnable in an `MdcRunnableWrapper` so that trace IDs
are propagated across all classes and boundaries. Crucial for observing job failures and requests.
Context is cleared to prevent data leaks.

**Structured JSON logging**

All logs emit JSON via a custom Logback encoder with `traceId` and `user` MDC fields,
so they're queryable without parsing. Single unified log appender `JSON_APP` for consistent
log formatting throughout. Root level logger is set to info preventing unneeded noise.

**Shared link permissions**

Each created link must follow explicit permissions from `shared_actions`. A link cannot endow
permissions that have not been placed in the set. The default permission is `READ`. However, if
the set only contains `UPDATE`, no users will be able to access that link.

**Integration testing for core invariants**

Integration tests use a mockMvc to simulate real request flows and `@Autowired` fields. Created
tests were meant to preserve core business logic that cannot shift.

**Tag normalization**

Duplicate tags are not regenerated. Tags are defaulted to lowercase to prevent case based duplication.
Deleting a tag only removes it from that note as other notes may have the same tag.

**Pagination**

Searching a note returns a `PageResponse<T>`

---

## Tech stack

- Java 21 / Spring Boot 3
- Spring Security + JJWT
- PostgreSQL + Flyway
- Docker

---

## Running locally
```bash
# Start the database
docker compose up db

# Run the app (dev profile connects to localhost:5432)
./mvnw spring-boot:run

# Data persists across restarts via Docker volume-backed PostgreSQL.

# API available at http://localhost:8081
```

**Production (full stack):**
```bash
docker compose up --build
# App available at http://localhost:8082
```

---

## What's intentionally incomplete

`OwnerAuthorization` is stubbed. It authorizes all actions for now.
It's designed to be the place where role-based or resource-level rules would live.

`EmailJobService` doesn't send a real email. It sends an example request to a fake api.
It's meant to be replaced with SMTP in the future.

