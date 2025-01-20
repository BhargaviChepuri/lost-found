
package com.mss.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mss.demo.dto.CategoriesDTO;
import com.mss.demo.entity.Categories;

public interface CategoriesRepo extends JpaRepository<Categories, Integer> {

	Optional<Categories> findByNameIn(List<String> names);

	
	Categories findByName(String categoryName);
	
	@Query("SELECT new com.mss.demo.dto.CategoriesDTO(c.id, c.name) FROM Categories c")
	List<CategoriesDTO> findCategoriesWithFields();


	 @Query("SELECT c.name FROM Categories c WHERE c.id = :categoryId")
	 String findCategoryNameById(@Param("categoryId") int categoryId);


	 boolean existsByNameIgnoreCase(String term);	
	 
	 Optional<Categories> findByNameIgnoreCase(String name);

}
