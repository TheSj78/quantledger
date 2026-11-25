package com.purdue.quantledger.controller;

import com.purdue.quantledger.model.SimulationRun;
import com.purdue.quantledger.model.TradeLedger;
import com.purdue.quantledger.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/simulate/{strategyId}")
    public ResponseEntity<SimulationRun> simulateStrategy(
            @PathVariable Long strategyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end   = LocalDate.parse(endDate);
        return ResponseEntity.ok(reportService.runSimulation(strategyId, start, end));
    }

    @GetMapping("/runs/{strategyId}")
    public ResponseEntity<List<SimulationRun>> getRunsForStrategy(
            @PathVariable Long strategyId) {
        return ResponseEntity.ok(reportService.getRunsForStrategy(strategyId));
    }

    @GetMapping("/trades/{runId}")
    public ResponseEntity<List<TradeLedger>> getTradesForRun(
            @PathVariable Long runId) {
        return ResponseEntity.ok(reportService.getTradesForRun(runId));
    }

    /**
     * Date-range simulation history endpoint.
     * Supports the "Date Range" filter on the Reports Page.
     * Uses REPEATABLE_READ isolation (see ReportService.getRunsForStrategyInDateRange).
     * Backed by idx_simulation_runs_strategy_id (Index 3) and
     * idx_trade_ledger_execution_date (Index 5).
     */
    @GetMapping("/runs/{strategyId}/range")
    public ResponseEntity<List<SimulationRun>> getRunsInDateRange(
            @PathVariable Long strategyId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end   = LocalDate.parse(endDate);
        return ResponseEntity.ok(
                reportService.getRunsForStrategyInDateRange(strategyId, start, end));
    }
}
