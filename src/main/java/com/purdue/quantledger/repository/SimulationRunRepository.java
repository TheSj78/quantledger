package com.purdue.quantledger.repository;

import com.purdue.quantledger.model.SimulationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SimulationRunRepository extends JpaRepository<SimulationRun, Long> {

    // Derived query — Spring Data translates this into a parameterized SQL
    // statement automatically, preventing SQL injection.
    List<SimulationRun> findByStrategyId(Long strategyId);

    /**
     * Date-range query used by the "Simulation History" report on the Reports Page.
     *
     * SQL Injection protection: All bind variables (:strategyId, :start, :end)
     * are passed as named parameters via @Param, which causes Spring Data /
     * Hibernate to emit a JDBC PreparedStatement.  User-supplied values are
     * never string-concatenated into the query text.
     *
     * Index support: This query benefits from idx_simulation_runs_strategy_id
     * (on strategy_id) to narrow the row set, and the WHERE clause on
     * start_date / end_date is aided by a potential composite index extension.
     */
    @Query("SELECT r FROM SimulationRun r " +
           "WHERE r.strategy.id = :strategyId " +
           "  AND r.startDate >= :start " +
           "  AND r.endDate   <= :end " +
           "ORDER BY r.startDate ASC")
    List<SimulationRun> findRunsByStrategyAndDateRange(
            @Param("strategyId") Long strategyId,
            @Param("start")      LocalDate start,
            @Param("end")        LocalDate end);
}
