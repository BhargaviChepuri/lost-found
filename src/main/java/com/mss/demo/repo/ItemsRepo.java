
package com.mss.demo.repo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mss.demo.dto.ItemDTO;
import com.mss.demo.dto.ItemStatus;
import com.mss.demo.dto.ItemSummaryDTO;
import com.mss.demo.dto.ItemsSearchDTO;
import com.mss.demo.entity.Items;

public interface ItemsRepo extends JpaRepository<Items, Integer> {


	@Query("SELECT new com.mss.demo.dto.ItemsSearchDTO( "
			+ "i.itemId, i.receivedDate, i.expirationDate, i.colour, i.detectedText, "
			+ "i.orgId, i.description, i.title, i.itemName, i.status, "
			+ "u.userId, i.image, u.name, u.email, c.categoryName) " + "FROM Items i "
			+ "LEFT JOIN i.user u " + "LEFT JOIN Categories c ON c.id = i.categoryId "
			+ "WHERE (:itemName IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :itemName, '%'))) "
			+ "AND (:color IS NULL OR LOWER(i.colour) LIKE LOWER(CONCAT('%', :color, '%'))) "
			+ "AND (:category IS NULL OR LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :category, '%')))")
	List<ItemsSearchDTO> searchByItemNameColorAndCategory(@Param("itemName") String itemName,
			@Param("color") String color, @Param("category") String category);

	List<Items> findByOrgId(String orgId);

	List<Items> findByExpirationDateBefore(Date expirationDate);

	List<Items> findByExpirationDateBetween(Date startDate, Date endDate);

	List<Items> findByStatusIn(List<String> statuses);

	@Query("SELECT new com.mss.demo.dto.ItemSummaryDTO("
			+ "i.itemId, i.receivedDate, i.status, i.description, i.image, i.uniqueId, c.categoryName, i.itemName) "
			+ "FROM Items i " + "LEFT JOIN Categories c ON i.categoryId = c.id " + "WHERE i.status != 'ARCHIEVED' "
			+ "ORDER BY i.receivedDate DESC") // Sort by number part as a string
	List<ItemSummaryDTO> findItemsSummaryByDate();

	@Query("SELECT new com.mss.demo.dto.ItemSummaryDTO("
			+ "i.itemId, i.receivedDate, i.status, i.description, i.image, i.uniqueId, c.categoryName, i.itemName) "
			+ "FROM Items i " + "LEFT JOIN Categories c ON i.categoryId = c.id " + "WHERE i.status != 'ARCHIVED' "
			+ "ORDER BY i.itemId DESC")
	List<ItemSummaryDTO> findItemsSummary();

	@Query("SELECT new com.mss.demo.dto.ItemDTO(i.itemId, i.itemName, i.status, i.receivedDate, u.userId, i.image, u.name, u.email, c.categoryName ) "
			+ "FROM Items i " + "LEFT JOIN i.user u " + "LEFT JOIN Categories c ON c.id = i.categoryId "
			+ "WHERE (:userId IS NULL OR u.userId = :userId) " + "AND (:status IS NULL OR i.status = :status) "
			+ "AND i.status != 'ARCHIVED' " + "AND (:receivedDate IS NULL OR i.receivedDate = :receivedDate)")
	List<ItemDTO> findAllByCriteria(@Param("userId") Integer userId, @Param("status") ItemStatus status,
			@Param("receivedDate") Date receivedDate);

	@Query("UPDATE Items i SET i.status = :status WHERE i.itemId = :itemId")
	void updateItemStatus(@Param("status") ItemStatus status, @Param("itemId") int itemId);

	@Query("SELECT FUNCTION('DATE_FORMAT', i.receivedDate, '%Y-%m') AS month, " + "COUNT(i) AS totalItems, "
			+ "COUNT(CASE WHEN i.status = 'UNCLAIMED' THEN 1 END) AS unclaimed, "
			+ "COUNT(CASE WHEN i.status = 'PENDING_APPROVAL' THEN 1 END) AS pendingApproval, "
			+ "COUNT(CASE WHEN i.status = 'PENDING_PICKUP' THEN 1 END) AS pendingPickup, "
			+ "COUNT(CASE WHEN i.status = 'CLAIMED' THEN 1 END) AS claimed, "
			+ "COUNT(CASE WHEN i.status = 'REJECTED' THEN 1 END) AS rejected " + "FROM Items i "
			+ "GROUP BY FUNCTION('DATE_FORMAT', i.receivedDate, '%Y-%m')")
	List<Object[]> findItemsGroupedByMonth();

	@Query("SELECT FUNCTION('DATE_FORMAT', i.receivedDate, '%Y-%m') AS month, " + "COUNT(i) AS totalItems, "
			+ "COUNT(CASE WHEN i.status = 'UNCLAIMED' THEN 1 END) AS unclaimed, "
			+ "COUNT(CASE WHEN i.status = 'PENDING_APPROVAL' THEN 1 END) AS pendingApproval, "
			+ "COUNT(CASE WHEN i.status = 'PENDING_PICKUP' THEN 1 END) AS pendingPickup, "
			+ "COUNT(CASE WHEN i.status = 'CLAIMED' THEN 1 END) AS claimed, "
			+ "COUNT(CASE WHEN i.status = 'REJECTED' THEN 1 END) AS rejected, "
			+ "COUNT(CASE WHEN i.status = 'ARCHIVED' THEN 1 END) AS archived " + "FROM Items i "
			+ "WHERE (:month IS NULL OR FUNCTION('DATE_FORMAT', i.receivedDate, '%Y-%m') = :month) " + "GROUP BY month "
			+ "ORDER BY month DESC")
	List<Object[]> findItemsGroupedByMonth(@Param("month") String month);

	@Query("SELECT i.categoryId, COUNT(i) FROM Items i GROUP BY i.categoryId")
	List<Object[]> findItemCountGroupedByCategoryId();

	@Query("SELECT FUNCTION('MONTH', i.receivedDate) AS month, i.categoryId, COUNT(i) AS totalItems " + "FROM Items i "
			+ "GROUP BY FUNCTION('MONTH', i.receivedDate), i.categoryId")
	List<Object[]> findItemCountGroupedByCategoryIdAndMonth();

	@Query("SELECT i.categoryId, COUNT(i) AS totalItems "
			+ "FROM Items i WHERE YEAR(i.receivedDate) = :year AND MONTH(i.receivedDate) = :month "
			+ "GROUP BY i.categoryId")
	List<Object[]> findItemCountGroupedByCategoryIdAndMonth(@Param("year") int year, @Param("month") int month);

	List<Items> findByStatus(ItemStatus status);

	@Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.uniqueId, LENGTH(:date) + 2) AS int)), 0) "
			+ "FROM Items i WHERE i.uniqueId LIKE CONCAT(:date, '-%')")
	Optional<Integer> findLatestNumberByDate(@Param("date") String date);

	@Modifying
	@Query("UPDATE Items i SET i.status = :status WHERE i.itemId = :itemId")
	int updateStatusById(@Param("status") ItemStatus status, @Param("itemId") Integer itemId);


}
