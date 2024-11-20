package com.mss.demo.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mss.demo.entity.Items;

public interface ItemsRepo extends JpaRepository<Items, Integer> {

	Page<Items> findByItemNameContainingIgnoreCase(String searchTerm, Pageable pageable);

	@Query("DELETE FROM Items i WHERE i.itemId IN :itemIds")
	void deleteItemsByIds(List<Integer> itemIds);

}
