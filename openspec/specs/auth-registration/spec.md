# auth-registration Specification

## Purpose
TBD - created by archiving change add-per-user-tenant-ownership. Update Purpose after archive.
## Requirements
### Requirement: Public self-service registration endpoint
The backend SHALL expose `POST /api/register` that accepts a JSON body `{username, password}` without any authentication header, creates a new `USER` account, and returns a signed JWT so the new user can act immediately. No admin approval step SHALL be required.

#### Scenario: Successful registration returns a JWT and creates an enabled USER
- **WHEN** the client posts a valid body with a unique `username` (>=3 chars) and `password` (>=6 chars) to `/api/register`
- **THEN** the response SHALL be `200` with a JSON body containing `jwt`, and a new `User` row SHALL be created with `role='USER'`, `enabled=true`, and a bcrypt-hashed password

#### Scenario: Duplicate username is rejected
- **WHEN** the client posts a body whose `username` already exists in the `User` table
- **THEN** the response SHALL be `400` with an error message indicating the username already exists, and no new row SHALL be created

#### Scenario: Invalid input is rejected by validation
- **WHEN** the body is missing `username`, missing `password`, has `username` shorter than 3 characters, or has `password` shorter than 6 characters
- **THEN** the response SHALL be `400` with a validation error and no new row SHALL be created

#### Scenario: Returned JWT lets the new user act immediately
- **WHEN** a successful registration response is received
- **THEN** the returned `jwt` SHALL be valid for the same set of endpoints any other USER-role JWT is valid for (notably `/api/me/*`), without any further interaction

### Requirement: Frontend SHALL provide a public registration page
The admin SPA SHALL include a `/register` route that is reachable without a token, presents a username/password/confirm form, submits to `POST /api/register`, stores the returned JWT, and navigates the new user to the dashboard.

#### Scenario: Unauthenticated visitor can reach /register
- **WHEN** a browser without any token in `localStorage` visits `#/register`
- **THEN** the registration form SHALL render (it MUST NOT be wrapped by an auth guard that redirects to `/login`)

#### Scenario: Successful registration auto-logs the user in
- **WHEN** a visitor submits a valid registration form
- **THEN** the SPA SHALL store the returned JWT via `setToken` and navigate to `/`, where the USER dashboard renders

#### Scenario: Login page links to the registration page
- **WHEN** a visitor on `/login` clicks "Đăng ký"
- **THEN** the SPA SHALL navigate to `/register` via a `react-router-dom` `<Link>` (no toast intercept)

