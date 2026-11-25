# QuantLedger

QuantLedger is a full-stack algorithmic trading strategy manager and backtest simulation reporting application built with **Spring Boot**, **PostgreSQL**, and **Angular** (Purdue CS 348).

---

## Features

- **Strategy Manager (`/strategies`)**: Create, edit, delete, and filter algorithmic trading strategies by user and stock ticker symbol (`initial_budget`, `buy_rule`, `sell_rule`).
- **Backtest Reports (`/reports`)**: Select a strategy and date range to run a simulated backtest, view computed final balance and total return percentage, and inspect the generated `BUY`/`SELL` trade ledger.
- **Database Schema Explorer (`/schema`)**: Interactive reference view displaying the relational schema, column types, and primary/foreign key relationships.
- **How It Works (`/how-to`)**: Walkthrough of the application workflow and architecture.

---

## Tech Stack

- **Backend**: Java 21, Spring Boot 4.0.1 (Spring Web, Spring Data JPA), Maven (`mvnw`)
- **Database**: PostgreSQL
- **Frontend**: Angular 21 (Standalone Components), TypeScript 5.9, RxJS

---

## Database Design & Performance

The schema is defined in [`src/main/resources/schema.sql`](src/main/resources/schema.sql) and seeded on startup via [`src/main/resources/data.sql`](src/main/resources/data.sql).

### Tables

| Table | Purpose | Key Relationships |
| :--- | :--- | :--- |
| `users` | Trader/researcher accounts (`user_id`, `username`, `email`, `created_at`) | Referenced by `strategies.user_id` |
| `tickers` | Market symbols (`symbol`, `company_name`, `sector`) | Referenced by `strategies.symbol` |
| `strategies` | Trading rules and initial budget (`strategy_id`, `name`, `initial_budget`, `buy_rule`, `sell_rule`) | FK to `users` & `tickers` (`ON DELETE CASCADE`) |
| `simulation_runs` | Backtest run summaries (`run_id`, `start_date`, `end_date`, `final_balance`, `total_return_pct`) | FK to `strategies` (`ON DELETE CASCADE`) |
| `trade_ledger` | Individual trade executions (`trade_id`, `trade_type`, `execution_date`, `price`, `quantity`) | FK to `simulation_runs` (`ON DELETE CASCADE`) |

### Indexes

- `idx_strategies_user_id` on `strategies(user_id)` — accelerates user-filtered strategy queries
- `idx_strategies_symbol` on `strategies(symbol)` — accelerates ticker-filtered strategy queries
- `idx_simulation_runs_strategy_id` on `simulation_runs(strategy_id)` — accelerates simulation history lookups
- `idx_trade_ledger_run_id` on `trade_ledger(run_id)` — accelerates trade ledger drill-down queries
- `idx_trade_ledger_execution_date` on `trade_ledger(execution_date)` — accelerates date-range trade filtering

### Transaction Isolation & Security

- **SQL Injection Protection**: All filtered and date-range repository queries use Spring Data JPA derived methods or parameterized JPQL `@Query` with `@Param` bind variables (JDBC `PreparedStatement`).
- **Transaction Boundaries**:
  - Strategy CRUD and simulation creation run inside `READ_COMMITTED` transactions so multi-step operations (e.g., persisting a `SimulationRun`, batch-saving `TradeLedger` rows, and updating final balances) commit or roll back atomically.
  - Date-range simulation reporting uses `REPEATABLE_READ` (`readOnly = true`) for consistent snapshot reads in PostgreSQL.

---

## API Endpoints

Base URL: `http://localhost:8080`

### Strategies (`/api/strategies`)
- `GET /api/strategies?userId={id}&ticker={symbol}` — List strategies (optional `userId` and `ticker` filters)
- `GET /api/strategies/{id}` — Get strategy by ID
- `POST /api/strategies` — Create a new strategy
- `PUT /api/strategies/{id}` — Update an existing strategy
- `DELETE /api/strategies/{id}` — Delete a strategy (cascades to simulation runs and trades)

### Reports (`/api/reports`)
- `POST /api/reports/simulate/{strategyId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` — Run a simulation and persist trades
- `GET /api/reports/runs/{strategyId}` — List all simulation runs for a strategy
- `GET /api/reports/runs/{strategyId}/range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` — List simulation runs within a date range
- `GET /api/reports/trades/{runId}` — List all trade ledger entries for a simulation run

### Dropdowns (`/api/dropdowns`)
- `GET /api/dropdowns/users` — List all users
- `GET /api/dropdowns/tickers` — List all tickers

---

## Project Structure

```text
QuantLedger/
├── pom.xml                          # Maven build configuration (Spring Boot 4.0.1, Java 21)
├── .env.example                     # Sample environment variables for PostgreSQL
├── src/main/java/com/purdue/quantledger/
│   ├── QuantLedgerApplication.java  # Spring Boot entry point
│   ├── controller/                  # REST controllers (Strategy, Report, Dropdown)
│   ├── service/                     # Transactional business logic & simulation engine
│   ├── repository/                  # Spring Data JPA repositories
│   └── model/                       # JPA entities
├── src/main/resources/
│   ├── application.properties       # App config (reads .env via spring.config.import)
│   ├── schema.sql                   # PostgreSQL tables & indexes
│   └── data.sql                     # Seed data (users, tickers, sample strategies)
└── frontend/                        # Angular 21 SPA
    ├── package.json
    └── src/app/
        ├── components/              # strategies-page, reports-page, schema-page, how-to-page
        ├── services/                # HTTP API services
        └── models/                  # TypeScript interfaces
```

---

## Local Setup

### Prerequisites
- **Java 21+**
- **Node.js & npm** (for Angular CLI)
- **PostgreSQL** running locally on port `5432`

### 1. Create the Database
```sql
CREATE DATABASE quantledger_db;
```

### 2. Configure Environment Variables
Copy `.env.example` to `.env` in the project root and set your PostgreSQL credentials:
```bash
cp .env.example .env
```
```ini
DB_URL=jdbc:postgresql://localhost:5432/quantledger_db
DB_USERNAME=your_postgres_username
DB_PASSWORD=your_postgres_password
```
Spring Boot automatically loads `.env` via `spring.config.import=optional:file:.env[.properties]` in `application.properties`.

### 3. Run the Backend
```bash
./mvnw spring-boot:run
```
The API server starts on `http://localhost:8080`. On startup, JPA updates the schema and `data.sql` seeds initial users, tickers, and sample strategies.

### 4. Run the Frontend
```bash
cd frontend
npm install
npm start
```
Open `http://localhost:4200` in your browser.
