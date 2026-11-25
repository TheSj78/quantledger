package com.purdue.quantledger.service;

import com.purdue.quantledger.model.Ticker;
import com.purdue.quantledger.model.User;
import com.purdue.quantledger.repository.TickerRepository;
import com.purdue.quantledger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DropdownService {

    private final UserRepository userRepository;
    private final TickerRepository tickerRepository;

    @Autowired
    public DropdownService(UserRepository userRepository, TickerRepository tickerRepository) {
        this.userRepository = userRepository;
        this.tickerRepository = tickerRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Ticker> getAllTickers() {
        return tickerRepository.findAll();
    }
}
