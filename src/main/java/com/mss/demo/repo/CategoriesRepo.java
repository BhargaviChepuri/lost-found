
package com.mss.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mss.demo.entity.Categories;

public interface CategoriesRepo extends JpaRepository<Categories, Integer> {

	Optional<Categories> findByCategoryNameIn(List<String> names);

	Categories findByCategoryName(String categoryName);

	boolean existsByCategoryNameIgnoreCase(String term);


	@Query("SELECT c.id, c.categoryName FROM Categories c")
	List<Object[]> findCategoryIdAndName();
}
