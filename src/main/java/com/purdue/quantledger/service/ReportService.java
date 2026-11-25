package com.purdue.quantledger.service;

import com.purdue.quantledger.model.SimulationRun;
import com.purdue.quantledger.model.Strategy;
import com.purdue.quantledger.model.TradeLedger;
import com.purdue.quantledger.repository.SimulationRunRepository;
import com.purdue.quantledger.repository.StrategyRepository;
import com.purdue.quantledger.repository.TradeLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.temporal.ChronoUnit;

@Service
public class ReportService {

    private final SimulationRunRepository simulationRunRepository;
    private final TradeLedgerRepository tradeLedgerRepository;
    private final StrategyRepository strategyRepository;

    @Autowired
    public ReportService(SimulationRunRepository simulationRunRepository,
                         TradeLedgerRepository tradeLedgerRepository,
                         StrategyRepository strategyRepository) {
        this.simulationRunRepository = simulationRunRepository;
        this.tradeLedgerRepository   = tradeLedgerRepository;
        this.strategyRepository      = strategyRepository;
    }

    /**
     * Runs a trading simulation and persists the result atomically.
     *
     * Isolation level: READ_COMMITTED
     *   - Each statement sees only rows committed before it began; dirty reads
     *     (reading another transaction's uncommitted changes) are prevented.
     *   - We chose READ_COMMITTED (PostgreSQL's default) rather than
     *     SERIALIZABLE because:
     *       1. Simulation runs are independent per strategy — two concurrent
     *          simulations on different strategies cannot interfere.
     *       2. We don't need the full overhead of SERIALIZABLE isolation for
     *          this write-heavy operation.
     *   - The entire operation (save SimulationRun + save all TradeLedger rows
     *     + update final balance) is wrapped in a single transaction so that a
     *     crash mid-way leaves the database in a consistent state (no orphan
     *     partial runs).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public SimulationRun runSimulation(Long strategyId, LocalDate startDate, LocalDate endDate) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new RuntimeException("Strategy not found"));

        // Dummy simulation logic for CS 348 Req 2 report generation
        BigDecimal currentBalance = strategy.getInitialBudget();
        Random rand = new Random();
        int numTrades = rand.nextInt(10) + 5; // 5 to 14 trades

        SimulationRun run = new SimulationRun();
        run.setStrategy(strategy);
        run.setStartDate(startDate);
        run.setEndDate(endDate);

        // Placeholder values; will be overwritten after trade generation
        run.setFinalBalance(currentBalance);
        run.setTotalReturnPct(BigDecimal.ZERO);
        run = simulationRunRepository.save(run);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween <= 0) daysBetween = 1;

        List<TradeLedger> trades = new ArrayList<>();
        for (int i = 0; i < numTrades; i++) {
            TradeLedger trade = new TradeLedger();
            trade.setSimulationRun(run);
            trade.setTradeType(i % 2 == 0 ? "BUY" : "SELL");
            trade.setExecutionDate(startDate.plusDays(rand.nextInt((int) daysBetween)));
            BigDecimal price = BigDecimal.valueOf(150 + rand.nextInt(50));
            trade.setPrice(price);
            int quantity = rand.nextInt(10) + 1;
            trade.setQuantity(quantity);

            if (trade.getTradeType().equals("BUY")) {
                currentBalance = currentBalance.subtract(price.multiply(BigDecimal.valueOf(quantity)));
            } else {
                currentBalance = currentBalance.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
            trades.add(trade);
        }

        tradeLedgerRepository.saveAll(trades);

        // Finalize run with computed values
        run.setFinalBalance(currentBalance);
        BigDecimal returnPct = currentBalance.subtract(strategy.getInitialBudget())
                .divide(strategy.getInitialBudget(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        run.setTotalReturnPct(returnPct);

        return simulationRunRepository.save(run);
    }

    /**
     * Fetches all simulation runs for a strategy.
     *
     * Isolation level: READ_COMMITTED (read-only)
     *   - readOnly = true is a hint to the JPA provider and the JDBC driver
     *     to skip dirty-checking, flush, and any write-locking — giving a
     *     small performance gain on pure reads.
     *   - READ_COMMITTED prevents us from seeing another transaction's
     *     in-flight (uncommitted) simulation data, which keeps the report
     *     output consistent.
     *   - Supports: "Simulation History" on the Reports Page
     *     (SimulationRunRepository.findByStrategyId — aided by
     *     idx_simulation_runs_strategy_id, Index 3).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<SimulationRun> getRunsForStrategy(Long strategyId) {
        return simulationRunRepository.findByStrategyId(strategyId);
    }

    /**
     * Fetches all individual trades for a given simulation run.
     *
     * Isolation level: READ_COMMITTED (read-only)
     *   - Same rationale as getRunsForStrategy: pure read, no dirty reads,
     *     lightweight.
     *   - Supports: "Trade Ledger Drill-Down" table on the Reports Page
     *     (TradeLedgerRepository.findBySimulationRunId — aided by
     *     idx_trade_ledger_run_id, Index 4).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<TradeLedger> getTradesForRun(Long runId) {
        return tradeLedgerRepository.findBySimulationRunId(runId);
    }

    /**
     * Date-range simulation report — supports the date-range filter on the
     * Reports Page.
     *
     * Isolation level: REPEATABLE_READ
     *   - This report re-reads the simulation_runs rows more than once (first
     *     to fetch runs, then again when building trade summaries), so we
     *     upgrade to REPEATABLE_READ to guarantee that the same rows are seen
     *     across both reads even if another transaction commits in between.
     *   - In PostgreSQL, REPEATABLE_READ is implemented via MVCC snapshots,
     *     so it adds no extra locking overhead — just a consistent snapshot.
     *   - Supports idx_simulation_runs_strategy_id (Index 3) and
     *     idx_trade_ledger_execution_date (Index 5).
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ, readOnly = true)
    public List<SimulationRun> getRunsForStrategyInDateRange(
            Long strategyId, LocalDate start, LocalDate end) {
        return simulationRunRepository.findRunsByStrategyAndDateRange(strategyId, start, end);
    }
}
