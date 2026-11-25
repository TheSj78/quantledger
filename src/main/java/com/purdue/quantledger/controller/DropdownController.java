package com.purdue.quantledger.controller;

import com.purdue.quantledger.model.Ticker;
import com.purdue.quantledger.model.User;
import com.purdue.quantledger.service.DropdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dropdowns")
@CrossOrigin(origins = "http://localhost:4200")
public class DropdownController {

    private final DropdownService dropdownService;

    @Autowired
    public DropdownController(DropdownService dropdownService) {
        this.dropdownService = dropdownService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(dropdownService.getAllUsers());
    }

    @GetMapping("/tickers")
    public ResponseEntity<List<Ticker>> getAllTickers() {
        return ResponseEntity.ok(dropdownService.getAllTickers());
    }
}
