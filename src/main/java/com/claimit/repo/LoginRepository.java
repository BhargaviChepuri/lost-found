package com.claimit.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.claimit.entity.Login;

public interface LoginRepository extends JpaRepository<Login, Long> {
    Login findByEmail(String email);

}
