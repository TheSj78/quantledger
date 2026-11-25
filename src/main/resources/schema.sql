-- Note: Since Hibernate ddl-auto=update is used, we don't necessarily need to create tables here if Hibernate does it, but providing this explicitly is good practice.

CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(200) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tickers (
    symbol VARCHAR(10) PRIMARY KEY,
    company_name VARCHAR(200) NOT NULL,
    sector VARCHAR(100)
);

-- Main CRUD table for Requirement 1
CREATE TABLE IF NOT EXISTS strategies (
    strategy_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    symbol VARCHAR(10) NOT NULL REFERENCES tickers(symbol) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    initial_budget NUMERIC(15, 2) NOT NULL,
    buy_rule VARCHAR(500),
    sell_rule VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS simulation_runs (
    run_id SERIAL PRIMARY KEY,
    strategy_id INT NOT NULL REFERENCES strategies(strategy_id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    final_balance NUMERIC(15, 2) NOT NULL,
    total_return_pct NUMERIC(10, 4) NOT NULL
);

CREATE TABLE IF NOT EXISTS trade_ledger (
    trade_id SERIAL PRIMARY KEY,
    run_id INT NOT NULL REFERENCES simulation_runs(run_id) ON DELETE CASCADE,
    trade_type VARCHAR(10) NOT NULL, -- BUY or SELL
    execution_date DATE NOT NULL,
    price NUMERIC(15, 2) NOT NULL,
    quantity INT NOT NULL
);

-- =============================================================================
-- INDEXES
-- =============================================================================

-- Index 1: strategies.user_id
--   Supports: GET /api/strategies?userId=X
--             -> StrategyRepository.findByFilters(@Param userId)
--             -> Feature: "Filter Strategies by User" on the Strategies Page.
--   Justification: Every strategy list request is scoped to a user.  Without
--   this index PostgreSQL performs a full sequential scan of strategies on each
--   request, which degrades linearly as strategies grow.
CREATE INDEX IF NOT EXISTS idx_strategies_user_id
    ON strategies (user_id);

-- Index 2: strategies.symbol (ticker)
--   Supports: GET /api/strategies?ticker=AAPL
--             -> StrategyRepository.findByFilters(@Param ticker)
--             -> Feature: "Filter Strategies by Ticker" dropdown on the
--                Strategies Page.
--   Justification: Users frequently filter strategies by the stock symbol they
--   trade.  A B-tree index on symbol makes these look-ups O(log n) rather than
--   O(n) sequential scans.
CREATE INDEX IF NOT EXISTS idx_strategies_symbol
    ON strategies (symbol);

-- Index 3: simulation_runs.strategy_id
--   Supports: GET /api/reports/runs/{strategyId}
--             -> SimulationRunRepository.findByStrategyId(strategyId)
--             -> Report: "Simulation History" panel on the Reports Page.
--   Justification: The report page fetches every simulation run for a chosen
--   strategy on each page load.  This index eliminates a full table scan of
--   simulation_runs, which can grow to many rows per strategy over time.
CREATE INDEX IF NOT EXISTS idx_simulation_runs_strategy_id
    ON simulation_runs (strategy_id);

-- Index 4: trade_ledger.run_id
--   Supports: GET /api/reports/trades/{runId}
--             -> TradeLedgerRepository.findBySimulationRunId(runId)
--             -> Report: "Trade Ledger Drill-Down" table on the Reports Page.
--   Justification: Retrieving individual trades for a given simulation run is
--   the hot path when rendering the trade-ledger table.  run_id is the natural
--   join/filter key and is used in every drill-down query.
CREATE INDEX IF NOT EXISTS idx_trade_ledger_run_id
    ON trade_ledger (run_id);

-- Index 5: trade_ledger.execution_date
--   Supports: SimulationRunRepository.findRunsByStrategyAndDateRange
--             -> date-range queries: execution_date BETWEEN :start AND :end
--             -> Report: "Date-Range Trade Report" on the Reports Page.
--   Justification: Date-range queries over large trade_ledger tables perform
--   poorly without this index.  A B-tree on execution_date lets PostgreSQL
--   jump directly to the matching date range instead of scanning every row.
CREATE INDEX IF NOT EXISTS idx_trade_ledger_execution_date
    ON trade_ledger (execution_date);
