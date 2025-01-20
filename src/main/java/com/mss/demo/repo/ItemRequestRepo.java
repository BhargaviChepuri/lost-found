
package com.mss.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mss.demo.dto.ItemStatus;
import com.mss.demo.dto.ItemsRequestDTO;
import com.mss.demo.entity.ItemsRequest;

public interface ItemRequestRepo extends JpaRepository<ItemsRequest, Integer> {

	boolean existsByUser_UserIdAndItem_ItemIdAndStatus(int userId, int itemId, ItemStatus status);

	Optional<ItemsRequest> findFirstByItem_ItemIdAndStatus(int itemId, ItemStatus status);

	List<ItemsRequest> findAllByItem_ItemIdAndStatus(int itemId, ItemStatus status);

	List<ItemsRequest> findByUser_UserId(int userId);

	
	@Query("SELECT new com.mss.demo.dto.ItemsRequestDTO( "
	        + "ir.requestId, ir.claimedDate as claimDate, ir.status, ir.item.image, ir.user.name, ir.user.email) "
	        + "FROM ItemsRequest ir "
	        + "WHERE ir.user.userId = :userId AND ir.status <> :excludedStatus")
	List<ItemsRequestDTO> findByUserIdAndExcludeStatus(@Param("userId") int userId,
	                                                   @Param("excludedStatus") ItemStatus excludedStatus);


	@Query("UPDATE ItemsRequest ir SET ir.status = :status WHERE ir.requestId = :requestId")
	void updateRequestStatus(@Param("status") ItemStatus status, @Param("requestId") int i);

	@Query("SELECT ir FROM ItemsRequest ir WHERE ir.item.itemId = :itemId")
	Optional<ItemsRequest> findByItemId(@Param("itemId") int itemId);
}
