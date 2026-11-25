package com.purdue.quantledger.repository;

import com.purdue.quantledger.model.TradeLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TradeLedgerRepository extends JpaRepository<TradeLedger, Long> {

    // Derived query — Spring Data emits a PreparedStatement with a bound
    // parameter for runId, preventing SQL injection.
    List<TradeLedger> findBySimulationRunId(Long runId);

    /**
     * Date-range trade report query — used by the "Trade Ledger Drill-Down"
     * feature on the Reports Page.
     *
     * SQL Injection protection: :runId and :startDate / :endDate are bound as
     * named parameters via @Param.  Hibernate sends these as JDBC bind
     * variables in a PreparedStatement — user input is NEVER concatenated
     * into the SQL string.
     *
     * Index support:
     *   - idx_trade_ledger_run_id        (Index 4) — filters on run_id first.
     *   - idx_trade_ledger_execution_date (Index 5) — accelerates the date
     *     range predicate, especially useful when a single run spans many days.
     */
    @Query("SELECT t FROM TradeLedger t " +
           "WHERE t.simulationRun.id = :runId " +
           "  AND t.executionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.executionDate ASC")
    List<TradeLedger> findTradesByRunAndDateRange(
            @Param("runId")      Long runId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate);
}
