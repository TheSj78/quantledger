package com.purdue.quantledger.repository;

import com.purdue.quantledger.model.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    List<Strategy> findByUserId(Long userId);

    @Query("SELECT s FROM Strategy s WHERE (:userId IS NULL OR s.user.id = :userId) AND (:ticker IS NULL OR s.ticker.symbol = :ticker)")
    List<Strategy> findByFilters(@Param("userId") Long userId, @Param("ticker") String ticker);
}
