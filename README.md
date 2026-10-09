# SmartJobTracker

HR login (from database/seed.sql):
- Username: hrmanager
- Password: Hr@12345
Candidate: janec / Candidate@123.

A portfolio-sized full-stack **job portal**. Candidates register, browse and search jobs,
apply, and track application status. HR users post and manage jobs, review applicants, and
update statuses. A local **AI Job Description Matcher** powered by Ollama compares a job
description against a candidate's skills.

Built as an independent project that reuses the layered architecture style of the reference
Hostel Management System (Controller → Service → Repository, DTOs, global exception handling,
JWT security, React SPA) without any hostel-specific logic.

**Status:** Stage 6 — the AI job matcher is implemented. Candidates can compare a supplied
skill list against a job description through a locally running Ollama model and see matching
skills, missing skills, suggestions, a summary, and an approximate match score. Stages 1
(domain), 2 (security), 3 (frontend shell), 4 (jobs & applications), and 5 (dashboards) are
complete.

---

## Tech Stack

| Layer    | Choice | Version |
|----------|--------|---------|
| Language | Java | 21 |
| Backend  | Spring Boot (Web MVC, Data JPA, Validation, Security) | 4.1.x |
| Auth     | JWT (jjwt) + BCrypt | jjwt 0.12.x |
| Database | MySQL | 8.x |
| Frontend | React + Vite; react-router-dom + axios | React 19 / Vite 8 |
| AI       | Ollama (local, no paid APIs) | e.g. `qwen3:4b` |

Versions were chosen after checking the local toolchain (Java 21.0.12, Maven 3.9.16,
Node 24, npm 11, Ollama 0.35.1) and the latest stable Spring Boot 4.x line (4.1.1).

---

## Planned Entities

| Entity | Key fields |
|--------|-----------|
| `User` | id, fullName, email (unique), username (unique), passwordHash, phone, role (`CANDIDATE`/`HR`/`ADMIN`), active, createdAt |
| `Job` | id, title, company, location, description, requiredSkills, employmentType, salaryMin, salaryMax, expiresAt, status (`DRAFT`/`PUBLISHED`/`CLOSED`), postedByUserId, createdAt, updatedAt |
| `JobApplication` | id, jobId, candidateId, coverLetter, status (`APPLIED`/`UNDER_REVIEW`/`SHORTLISTED`/`REJECTED`/`HIRED`), appliedAt, updatedAt |

Enums: `Role`, `JobStatus`, `ApplicationStatus`.

---

## Features

1. **Public landing page** — job search, featured/recent jobs, links to Candidate Login, HR
   Login, and registration.
2. **Candidate** — registration/login, job browsing + search, application submission, and
   application-status tracking.
3. **HR** — login and dashboard, full CRUD for job postings, applicant viewing, and
   application-status updates.
4. **CRUD + authorization** — jobs and applications with role-based access rules.
5. **Dashboard statistics** — aggregate counts for HR and candidates.
6. **AI Matcher** — compare a job description with candidate skills → matching skills,
   missing skills, learning suggestions, summary, and an approximate score (via Ollama).
   The score is clearly framed as an AI estimate, never a hiring probability.

---

## REST API

All stages are implemented. Endpoints below are grouped by area.

| Area | Method | Endpoint | Access |
|------|--------|----------|--------|
| Auth | POST | `/api/auth/register` | public — always creates `CANDIDATE` |
| Auth | POST | `/api/auth/login` | public — username **or** email |
| Auth | GET | `/api/auth/me` | authenticated — requires `Bearer` token |
| Jobs | GET | `/api/jobs` | public — paged (`keyword`, `location`, `type`, `page`, `size`, `sort`) |
| Jobs | GET | `/api/jobs/search` | public — same paged search as `/api/jobs` |
| Jobs | GET | `/api/jobs/{id}` | public — published jobs only |
| Jobs | POST | `/api/jobs` | `HR` |
| Jobs | PUT | `/api/jobs/{id}` | `HR` (owner) |
| Jobs | DELETE | `/api/jobs/{id}` | `HR` (owner) |
| Jobs | GET | `/api/hr/jobs` | `HR` — own postings, any status |
| Jobs | GET | `/api/hr/jobs/{id}` | `HR` (owner) |
| Apps | POST | `/api/jobs/{id}/applications` | `CANDIDATE` — apply |
| Apps | GET | `/api/applications/{id}` | owner candidate or the job's `HR` |
| Apps | DELETE | `/api/applications/{id}` | `CANDIDATE` (owner, withdraw unless final) |
| Apps | GET | `/api/candidates/applications` | `CANDIDATE` — own applications |
| Apps | GET | `/api/hr/jobs/{id}/applications` | `HR` (owner) — applicants |
| Apps | PATCH | `/api/hr/applications/{id}/status` | `HR` (owner) — guarded transitions |
| Dash | GET | `/api/candidates/dashboard` | `CANDIDATE` — own application stats |
| Dash | GET | `/api/hr/dashboard` | `HR` — own posting & applicant stats |
| AI | POST | `/api/ai/match` | `CANDIDATE` — skill/job-description match |

Application status transitions are validated server-side: `APPLIED → UNDER_REVIEW/SHORTLISTED/
REJECTED`, `UNDER_REVIEW → SHORTLISTED/REJECTED/HIRED`, `SHORTLISTED → UNDER_REVIEW/REJECTED/
HIRED`; `REJECTED` and `HIRED` are terminal (no further changes, and withdrawal is blocked).

Dashboard endpoints derive identity from the JWT principal (no candidate/HR id in the request),
aggregate in the database via repository queries, always return a full status breakdown
(including zero counts) plus deterministic recent-activity lists, and return zeroed stats with
empty lists when the user has no data.

`POST /api/ai/match` accepts `{ "skills": [...], "jobDescription": "..." }` and returns
`matchingSkills`, `missingSkills`, `improvementSuggestions`, `summary`, `matchScore` (0–100),
and `matchScoreNote`. The score is an **approximate AI-generated estimate**, not an objective
probability of being hired. When Ollama is down or times out the endpoint returns `503`; when
the model returns unparseable output it returns `502`.

---

## Architecture

**Backend** — layers: `Controller → Service → Repository`, with DTOs for all API input/output,
Bean Validation on requests, and a global exception handler. Authorization enforced on the
backend. AI integration isolated in the `ai` package behind its own service so it can be
disabled/swapped.

```
com.jobportal
├── controller   # REST endpoints
├── service      # business logic
├── dao          # data-access helpers (where needed)
├── repository   # Spring Data JPA repositories
├── entity       # JPA entities
├── dto          # request/response DTOs
├── config       # security, CORS, beans
├── security     # JWT filter, token provider, UserDetails
├── exception    # global handler + custom exceptions
└── ai           # isolated AI integration: OllamaClient (HTTP), AiMatcherService
                 # (strict prompt + safe parsing), AI exceptions + handler
```

**Frontend** — React SPA with routing, an auth context, a shared axios client, reusable
components, and one page folder per feature area.

```
frontend/src
├── components   # Navbar, Footer, ProtectedRoute, DashboardLayout, LoadingSpinner,
│                # ErrorMessage, JobCard, StatusBadge, StatusBreakdown
├── constants    # jobs (employment types, statuses, transitions, formatters)
├── pages        # LandingPage, LoginPage, RegisterPage, NotFoundPage,
│                # jobs/JobsPage, jobs/JobDetailPage,
│                # candidate/CandidateDashboard (live stats), candidate/CandidateApplications,
│                # candidate/AIJobMatcher,
│                # hr/HRDashboard (live stats), hr/HrJobs, hr/HrJobForm, hr/HrJobApplicants
├── services     # apiClient (axios), authService, jobService, applicationService,
│                # dashboardService, aiService, tokenStore, errorUtils
├── context      # AuthContext (provider) + useAuth hook
└── routes       # AppRoutes (route table) + roles
```

### Frontend routes

| Route | Page | Access |
|-------|------|--------|
| `/` | Landing (hero, live featured jobs, feature/CTA sections) | public |
| `/login` | Candidate login | public (redirects if already signed in) |
| `/hr/login` | HR login | public (redirects if already signed in) |
| `/register` | Candidate registration | public (candidate only) |
| `/jobs` | Job browse + filters + paging | public |
| `/jobs/:jobId` | Job detail + apply form | public (apply needs `CANDIDATE`) |
| `/candidate/dashboard` | Candidate dashboard (live stats + recent activity) | `CANDIDATE` |
| `/candidate/applications` | Application tracking + withdraw | `CANDIDATE` |
| `/candidate/ai-matcher` | AI job matcher (skills vs. description) | `CANDIDATE` |
| `/hr/dashboard` | Recruiter dashboard (live stats + pipeline) | `HR` |
| `/hr/jobs` | Manage job postings | `HR` |
| `/hr/jobs/new` | Create a posting | `HR` |
| `/hr/jobs/:jobId/edit` | Edit a posting | `HR` |
| `/hr/jobs/:jobId/applicants` | Review applicants + change status | `HR` |
| `*` | Not found | public |

`ProtectedRoute` is a UI-only guard: it redirects unauthenticated users to the matching login
page and prevents a candidate from opening HR routes (and vice-shape). The backend still
authorises every request, so the guard is a convenience, not the security boundary.

### Client-side token handling

The backend is stateless, so the frontend keeps the JWT in `localStorage` (via
`services/tokenStore.js`) to survive page reloads and attaches it as `Authorization: Bearer …`
on each request through an axios interceptor. **`localStorage` is readable by any script on the
page and does not protect against XSS**; a hardened deployment would use an HttpOnly cookie.
The token is never trusted on its own — `GET /api/auth/me` re-validates it on load and the
backend verifies signature, expiry, and role on every call. Passwords are never stored in the
browser.

---

## Security

- Spring Security + JWT, stateless (`SessionCreationPolicy.STATELESS`); BCrypt password
  hashing; CSRF disabled; form login and HTTP Basic disabled.
- HS256 tokens signed with `JWT_SECRET` (must be ≥ 32 bytes; the app fails fast if the resolved
  value is blank or too short). A **dev-only default** is set in `application.yml` so the app
  boots locally with no env setup — always override it in any real deployment. Token subject is
  the username; issuer is `app.jwt.issuer`; expiry is `app.jwt.expiration-minutes`.
- Roles `CANDIDATE`, `HR` and `ADMIN` (`ADMIN` reserved for future admin endpoints). Public
  registration creates `CANDIDATE` only — any client-supplied role is ignored.
- Login accepts username **or** email; unknown user and wrong password both return
  `401 {"detail":"Invalid username or password"}`.
- Unauthenticated access to a protected endpoint → `401 {"detail":"Authentication required"}`;
  authenticated but wrong role → `403 {"detail":"Access denied"}`.
- HR/admin accounts are seeded via `database/seed.sql` — never self-registered.
- `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` are all env-overridable. `application.yml`
  ships **dev-only defaults** so the app boots locally out of the box; override every one of
  them with environment variables before exposing the app anywhere.

---

## Database

MySQL database `smart_job_tracker`. Scripts:

- `database/schema.sql` — database + tables + foreign keys
- `database/seed.sql` — sample HR user, candidate, jobs, and an application

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p smart_job_tracker < database/seed.sql
```

Seed development logins (BCrypt hashes only are stored in `seed.sql`; change these anywhere
the app is exposed):

| Role      | Username    | Password       |
|-----------|-------------|----------------|
| HR        | `hrmanager` | `Hr@12345`     |
| Candidate | `janec`     | `Candidate@123`|

---

## Configuration

| Variable      | Default                                         | Notes |
|---------------|-------------------------------------------------|-------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/smart_job_tracker` | JDBC URL |
| `DB_USERNAME` | `root`                                          | DB user |
| `DB_PASSWORD` | `root`                                          | Dev-only default — override in production |
| `SERVER_PORT` | `8080`                                          | HTTP port |
| `JWT_SECRET`  | `dev-only-…-0123456789`                         | Dev-only default, ≥ 32 bytes — override in production |
| `JWT_EXPIRATION_MINUTES` | `120`                                | Token lifetime in minutes |
| `JWT_ISSUER`  | `smart-job-tracker`                             | JWT `iss` claim |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173`                | Comma-separated allowed origins |
| `OLLAMA_BASE_URL` | `http://localhost:11434`                     | Local Ollama base URL |
| `OLLAMA_MODEL` | `qwen3:4b`                                     | Model for the matcher (must be pulled locally) |
| `OLLAMA_CONNECT_TIMEOUT` | `5s`                                 | Ollama TCP connect timeout |
| `OLLAMA_READ_TIMEOUT` | `60s`                                    | Ollama response read timeout |
| `AI_MAX_JOB_DESCRIPTION_LENGTH` | `4000`                        | Max job-description characters accepted |
| `AI_MAX_SKILLS` | `50`                                          | Max skills accepted per request |
| `AI_MAX_SKILL_LENGTH` | `100`                                   | Max characters per skill |
| `OLLAMA_THINK` | `false`                                        | Disable reasoning output so reasoning models return JSON directly |
| `OLLAMA_MAX_OUTPUT_TOKENS` | `900`                            | Bound on generated tokens (`num_predict`) |

Backend tests run against in-memory H2 and mock/stub Ollama, so `mvn test` needs no MySQL and
no running model.

### Local AI model (Ollama)

The matcher talks only to a local Ollama server — no paid APIs and no data leaves the machine.

```bash
# 1. Install Ollama (https://ollama.com/download), then start the server
ollama serve            # http://localhost:11434

# 2. Pull a model that fits your machine
ollama pull qwen3:4b    # ~2.5 GB — good default for ~8 GB RAM
# ollama pull qwen3:8b  # ~5.2 GB — better quality, needs more RAM

# 3. Confirm what is installed, then point the app at it
ollama list
#   set OLLAMA_MODEL to the exact name shown, e.g. qwen3:4b
```

The app defaults to `qwen3:4b`. On this development machine (8 GB RAM) `qwen3:4b` fits
comfortably; `qwen3:8b` is also installed but is a tighter fit. The model name is never
hardcoded in business logic — swap it via `OLLAMA_MODEL` at any time.

`qwen3` is a reasoning model, so requests set `think: false` (via `OLLAMA_THINK`) to keep the
answer in Ollama's `response` field instead of burning the token budget on the thinking
channel. A request timeout (`OLLAMA_READ_TIMEOUT`) and an output-token cap
(`OLLAMA_MAX_OUTPUT_TOKENS`) keep local inference bounded. If Ollama is not running, the API
returns `503` instead of failing.

---

## Getting Started

Backend (needs a running MySQL):

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install      # installs React, React Router, and axios
npm run dev      # dev server on http://localhost:5173
npm run build    # production build into dist/
npm run lint     # oxlint
```

The dev server proxies `/api` to `http://localhost:8080`, so no CORS setup is needed locally.
Optionally copy `.env.example` to `.env` and adjust `VITE_API_URL` (defaults to `/api`).

---

## Implementation Plan

Each stage is built and verified before the next.

- [x] **Stage 0 — Skeleton + README**
- [x] **Stage 1 — Backend domain**: entities, enums, repositories, DTOs, validation, global
      exceptions, MySQL `schema.sql`/`seed.sql`, repository/service tests.
- [x] **Stage 2 — Security & JWT**: `UserDetails`, JWT provider/filter, `SecurityConfig`,
      candidate-only registration, HR role seeded, endpoint authorization rules.
- [x] **Stage 3 — Frontend shell**: routing, `AuthContext`, axios client, Navbar/Footer,
      landing page, candidate + HR login/registration pages, `ProtectedRoute`.
- [x] **Stage 4 — Jobs & applications**: job search/browse/detail, HR job CRUD, candidate
      apply + tracking, HR applicant management + guarded status transitions.
- [x] **Stage 5 — Dashboards**: candidate + HR statistics endpoints (database aggregation),
      live stat cards, status breakdowns, and recent-activity panels.
- [x] **Stage 6 — AI Matcher**: isolated Ollama client with timeouts, strict prompt +
      defensive output parsing, `POST /api/ai/match` (CANDIDATE), and the `AIJobMatcher` page.

---

## License

For portfolio / educational use.
