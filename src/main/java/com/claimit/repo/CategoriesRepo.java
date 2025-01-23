//
//package com.claimit.repo;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import com.claimit.dto.CategoriesDTO;
//import com.claimit.entity.Categories;
//
//public interface CategoriesRepo extends JpaRepository<Categories, Integer> {
//
////	Optional<Categories> findByNameIn(List<String> names);
//
//	
////	Categories findByName(String categoryName);
//	
//	@Query("SELECT new com.claimit.dto.CategoriesDTO(c.id, c.categoryName) FROM Categories c")
//	List<CategoriesDTO> findCategoriesWithFields();
//
//
//	 @Query("SELECT c.categoryName FROM Categories c WHERE c.id = :categoryId")
//	 String findCategoryNameById(@Param("categoryId") int categoryId);
//
//
//	 boolean existsByCategoryNameIgnoreCase(String term);	
//	 
////	 Optional<Categories> findBycategoryNameIgnoreCase(String name);
//
//}
