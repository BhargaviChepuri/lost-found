package com.claimit.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.claimit.entity.Login;

public interface LoginRepository extends JpaRepository<Login, Long> {

}
