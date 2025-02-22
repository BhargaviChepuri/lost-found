
package com.claimit.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.claimit.entity.Subcategories;

public interface SubCategoriesRepo extends JpaRepository<Subcategories, Integer> {

	
	@Query("SELECT s FROM Subcategories s WHERE LOWER(s.name) LIKE %:name%")
	List<Subcategories> findByNameLikeIgnoreCase(@Param("name") String name);
}
