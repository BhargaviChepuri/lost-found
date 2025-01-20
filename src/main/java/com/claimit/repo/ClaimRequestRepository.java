
package com.claimit.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.claimit.entity.ClaimRequest;

public interface ClaimRequestRepository extends JpaRepository<ClaimRequest, Integer> {

}

