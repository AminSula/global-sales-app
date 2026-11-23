# Sales Pilot

Admin panel + phone ordering (PWA) pilot. Spring Boot API in `backend/`, Angular app in `frontend/`.

## Requirements

- Java 17+ (and Maven, or IntelliJ IDEA which includes it)
- Node.js 22.22.3+
- PostgreSQL

## First run

1. Create an empty database:

   ```sql
   CREATE DATABASE sales_pilot;
   ```

2. Start the backend (creates all tables and the three accounts on first start):

   ```
   cd backend
   mvn spring-boot:run
   ```

   In IntelliJ: open `PilotApplication.java` and press the green Run arrow.

   Defaults: database `sales_pilot` on `localhost:5432`, user `postgres`, password `postgres`.
   Override with the environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD` (and `JWT_SECRET`).

3. Start the frontend:

   ```
   cd frontend
   npm install
   npm start
   ```

   Open http://localhost:4200. Calls to `/api` are forwarded to the backend on port 8080.

## Accounts (demo passwords)

| Username | Password  | Role          |
|----------|-----------|---------------|
| admin    | 123456    | Administrator |
| agent1   | 123456    | Sales agent   |
| agent2   | 123456    | Sales agent   |

## Trying it on a phone

Phone and computer on the same Wi-Fi:

```
cd frontend
npm run start:phone
```

Then open `http://<your-computer-ip>:4200` on the phone (find the IP with `ipconfig`).
This is enough to use the app in the phone's browser.

Installing it as an app (PWA, "Add to Home screen") needs HTTPS and the production build:
build the single deployable below, run the jar, and expose port 8080 through an HTTPS tunnel
(for example `cloudflared tunnel --url http://localhost:8080` or `ngrok http 8080`).

## Single deployable

One jar that contains the API and the built web app.

With Maven installed:

```
cd backend
mvn -Pfull clean package
java -jar target/sales-pilot-0.1.0.jar
```

Without Maven, in IntelliJ: Maven tool window → **Profiles** → tick `full` → **Lifecycle** →
run `clean`, then `package`. The first run downloads its own Node.js and takes a few minutes.

Everything is then served from http://localhost:8080. Stop the IntelliJ-run backend first,
because both use port 8080.

## Settings (environment variables)

| Variable | Meaning | Default |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Database connection | local `sales_pilot`, `postgres` / `postgres` |
| `JWT_SECRET` | Secret that signs login tokens (32+ characters) | a development value; the app warns at startup |
| `ADMIN_PASSWORD`, `AGENT_PASSWORD` | First passwords, read only when the accounts are created | `123456` |

## SQL scripts

- `backend/src/main/resources/db/migration/`: what the app runs itself on startup (Flyway).
  `V1` and `V2` are the stand-in for the client's existing tables; `V3` to `V5` are the app's own tables.
- `docs/sql/krijimi-i-tabelave.sql`: the app's own tables as one script, for the client. It produces
  exactly the same tables as the scripts above.
- `backend/sql/demo-data.sql`: demo data for testing by hand. Not part of the delivery.

## Documents

- `docs/UDHEZIME-INSTALIMI.md`: the short installation guide for the client (Albanian).
- `docs/ACCEPTANCE-CHECKLIST.md`: every point of the brief and how to show it in the app.
