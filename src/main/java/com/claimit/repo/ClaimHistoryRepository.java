
package com.claimit.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.claimit.dto.ClaimHistoryDTO;
import com.claimit.dto.ItemStatus;
import com.claimit.entity.ClaimHistory;

public interface ClaimHistoryRepository extends JpaRepository<ClaimHistory, Integer> {


	@Query("SELECT new com.claimit.dto.ClaimHistoryDTO( "
	        + "ch.claimId, ch.claimDate, ch.claimStatus, i.image, r.requestId, ch.user.id, ch.user.userName, ch.user.email) "
	        + "FROM ClaimHistory ch "
	        + "JOIN ch.request r "
	        + "JOIN r.item i "
	        + "WHERE (:email IS NULL OR ch.userEmail = :email) "
	        + "ORDER BY ch.claimDate DESC")
	List<ClaimHistoryDTO> findClaimHistoryByEmail(@Param("email") String email);


	String getUserEmailByClaimId(@Param("claimId") int claimId);

	Optional<ClaimHistory> findById(int claimId);

	Optional<ClaimHistory> findFirstByItemIdAndClaimStatus(int itemId, ItemStatus status);

	@Query("UPDATE ClaimHistory ch SET ch.claimStatus = :status WHERE ch.claimId = :claimId")
    void updateClaimStatus(@Param("status") ItemStatus status, @Param("claimId") int claimId);

}
