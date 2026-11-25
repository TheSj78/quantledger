package com.purdue.quantledger.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "strategies")
public class Strategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "strategy_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "symbol", nullable = false)
    private Ticker ticker;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "initial_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal initialBudget;

    @Column(name = "buy_rule", length = 500)
    private String buyRule;

    @Column(name = "sell_rule", length = 500)
    private String sellRule;

    @OneToMany(mappedBy = "strategy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SimulationRun> simulationRuns;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ticker getTicker() { return ticker; }
    public void setTicker(Ticker ticker) { this.ticker = ticker; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getInitialBudget() { return initialBudget; }
    public void setInitialBudget(BigDecimal initialBudget) { this.initialBudget = initialBudget; }
    public String getBuyRule() { return buyRule; }
    public void setBuyRule(String buyRule) { this.buyRule = buyRule; }
    public String getSellRule() { return sellRule; }
    public void setSellRule(String sellRule) { this.sellRule = sellRule; }
}
