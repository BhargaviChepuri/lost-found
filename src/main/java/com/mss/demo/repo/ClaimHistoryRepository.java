
package com.mss.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mss.demo.dto.ClaimHistoryDTO;
import com.mss.demo.dto.ItemStatus;
import com.mss.demo.entity.ClaimHistory;

public interface ClaimHistoryRepository extends JpaRepository<ClaimHistory, Integer> {

	List<ClaimHistory> findByRequest_Item_ItemId(int itemId);


	
	@Query("SELECT new com.mss.demo.dto.ClaimHistoryDTO( "
	        + "ch.claimId, ch.claimDate, ch.claimStatus, i.image, r.requestId, ch.user.id, ch.user.name, ch.user.email) "
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

	@Query("SELECT new com.mss.demo.dto.ClaimHistoryDTO(c.claimStatus, c.claimId, c.claimDate, it.image, ir.requestId, u.userId, u.name, u.email) " +
		       "FROM ClaimHistory c " +
		       "JOIN c.user u " +
		       "LEFT JOIN Items it ON c.itemId = it.id " +
		       "LEFT JOIN ItemsRequest ir ON c.request.requestId = ir.requestId")
		List<ClaimHistoryDTO> findAllClaimHistory();

}
