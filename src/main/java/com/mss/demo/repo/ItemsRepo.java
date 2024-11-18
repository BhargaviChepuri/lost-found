package com.mss.demo.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mss.demo.entity.Items;

public interface ItemsRepo extends JpaRepository<Items, Integer> {

	Page<Items> findByItemNameContainingIgnoreCase(String searchTerm, Pageable pageable);

}
