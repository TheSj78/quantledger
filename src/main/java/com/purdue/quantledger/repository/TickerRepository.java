package com.purdue.quantledger.repository;

import com.purdue.quantledger.model.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TickerRepository extends JpaRepository<Ticker, String> {
}
