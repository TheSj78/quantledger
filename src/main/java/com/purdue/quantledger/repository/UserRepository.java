package com.purdue.quantledger.repository;

import com.purdue.quantledger.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
