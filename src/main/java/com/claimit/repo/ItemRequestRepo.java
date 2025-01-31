
package com.claimit.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.claimit.dto.ItemStatus;
import com.claimit.dto.ItemsRequestDTO;
import com.claimit.entity.ItemsRequest;

public interface ItemRequestRepo extends JpaRepository<ItemsRequest, Integer> {

	Optional<ItemsRequest> findFirstByItem_ItemIdAndStatus(int itemId, ItemStatus status);

	@Query("SELECT new com.claimit.dto.ItemsRequestDTO( "
			+ "ir.requestId, ir.claimedDate as claimDate, ir.status, ir.item.image, ir.user.userName, ir.user.email) "
			+ "FROM ItemsRequest ir " + "WHERE ir.user.userId = :userId AND ir.status <> :excludedStatus")
	List<ItemsRequestDTO> findByUserIdAndExcludeStatus(@Param("userId") int userId,
			@Param("excludedStatus") ItemStatus excludedStatus);

	@Query("SELECT ir FROM ItemsRequest ir WHERE ir.item.itemId = :itemId")
	Optional<ItemsRequest> findByItemId(@Param("itemId") int itemId);
}
