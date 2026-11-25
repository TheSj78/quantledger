package com.purdue.quantledger.service;

import com.purdue.quantledger.model.Strategy;
import com.purdue.quantledger.repository.StrategyRepository;
import com.purdue.quantledger.repository.TickerRepository;
import com.purdue.quantledger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StrategyService {

    private final StrategyRepository strategyRepository;
    private final UserRepository     userRepository;
    private final TickerRepository   tickerRepository;

    @Autowired
    public StrategyService(StrategyRepository strategyRepository,
                           UserRepository userRepository,
                           TickerRepository tickerRepository) {
        this.strategyRepository = strategyRepository;
        this.userRepository     = userRepository;
        this.tickerRepository   = tickerRepository;
    }

    /**
     * Returns strategies, optionally filtered by userId and/or ticker.
     *
     * SQL Injection protection:
     *   The filter values (userId, ticker) are passed to the repository as
     *   named JPQL parameters (@Param) — see StrategyRepository.findByFilters.
     *   Spring Data / Hibernate translates these into JDBC PreparedStatement
     *   bind variables.  No string concatenation of user input ever touches
     *   the SQL text, so SQL injection attacks are structurally impossible.
     *
     * Transaction: READ_COMMITTED, readOnly = true — lightweight read.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public List<Strategy> getAllStrategies(Long userId, String ticker) {
        if (userId == null && (ticker == null || ticker.isEmpty())) {
            return strategyRepository.findAll();
        }
        return strategyRepository.findByFilters(
                userId,
                ticker != null && ticker.isEmpty() ? null : ticker);
    }

    /**
     * READ_COMMITTED, readOnly — single-row fetch by primary key.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public Optional<Strategy> getStrategyById(Long id) {
        return strategyRepository.findById(id);
    }

    /**
     * Creates a new strategy.
     *
     * Transaction: READ_COMMITTED write transaction.
     *   - Ensures the INSERT is atomic: if saving the strategy fails (e.g.,
     *     foreign-key violation on user_id or symbol), the whole transaction
     *     rolls back — no partial data is left in the database.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Strategy createStrategy(Strategy strategy) {
        return strategyRepository.save(strategy);
    }

    /**
     * Updates an existing strategy.
     *
     * Transaction: READ_COMMITTED write transaction.
     *   - The read-then-write pattern (findById → mutate → save) must be
     *     atomic.  Wrapping in a transaction prevents a lost-update anomaly
     *     where two concurrent updates could overwrite each other's changes.
     *   - READ_COMMITTED is sufficient here because we re-fetch the latest
     *     committed version of the row at the start of the transaction.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Strategy updateStrategy(Long id, Strategy strategyDetails) {
        return strategyRepository.findById(id).map(strategy -> {
            strategy.setName(strategyDetails.getName());
            strategy.setInitialBudget(strategyDetails.getInitialBudget());
            strategy.setBuyRule(strategyDetails.getBuyRule());
            strategy.setSellRule(strategyDetails.getSellRule());
            if (strategyDetails.getUser() != null) {
                userRepository.findById(strategyDetails.getUser().getId())
                              .ifPresent(strategy::setUser);
            }
            if (strategyDetails.getTicker() != null) {
                tickerRepository.findById(strategyDetails.getTicker().getSymbol())
                                .ifPresent(strategy::setTicker);
            }
            return strategyRepository.save(strategy);
        }).orElseThrow(() -> new RuntimeException("Strategy not found with id " + id));
    }

    /**
     * Deletes a strategy (and all cascading simulation runs and trades).
     *
     * Transaction: READ_COMMITTED write transaction.
     *   - The cascade delete (strategy → simulation_runs → trade_ledger) must
     *     be atomic; a failure mid-cascade would leave orphan rows without a
     *     transaction boundary.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteStrategy(Long id) {
        strategyRepository.deleteById(id);
    }
}
