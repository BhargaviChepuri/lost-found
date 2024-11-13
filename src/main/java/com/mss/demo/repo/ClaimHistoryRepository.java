package com.mss.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mss.demo.entity.ClaimHistory;

public interface ClaimHistoryRepository extends JpaRepository<ClaimHistory, Integer> {

}
