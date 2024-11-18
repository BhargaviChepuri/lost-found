package com.mss.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mss.demo.entity.ClaimHistory;

public interface ClaimHistoryRepository extends JpaRepository<ClaimHistory, Integer> {

	@Query("SELECT c FROM ClaimHistory c WHERE c.user.id = :userId")
	List<ClaimHistory> findByUserId(@Param("userId") int userId);

}
