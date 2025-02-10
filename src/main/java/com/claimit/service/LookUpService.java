package com.claimit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.claimit.entity.Categories;
import com.claimit.entity.LookUp;
import com.claimit.repo.CategoriesRepo;
import com.claimit.repo.LookUpRepository;

@Service
public class LookUpService {

	@Autowired
	private LookUpRepository lookUpRepository;

	@Autowired
	private CategoriesRepo categoriesRepo;

	/**
	 * Saves a category and its associated subcategories into the database.
	 * 
	 * This method is transactional and inserts both the category and its
	 * subcategories into the `look_up` table. Subclasses that override this method
	 * must ensure that the transactional behavior and SQL logic is respected.
	 * 
	 * @param categoryName  The name of the category to be saved.
	 * @param subcategories A list of subcategory names to be saved.
	 */

//	public LookUp addCategoryWithSubcategories(LookUp category) {
//		// Set type as CATEGORY for parent
//		category.setType("CATEGORY");
//
//		// If it has children, set their type as SUBCATEGORY and link them
//		if (category.getChildren() != null) {
//			for (LookUp subcategory : category.getChildren()) {
//				subcategory.setType("SUBCATEGORY");
//				subcategory.setParent(category);
//			}
//		}
//
//		// Save the category (CascadeType.ALL will handle children)
//		return lookUpRepository.save(category);
//	}

	public LookUp addCategoryWithSubcategories(LookUp category) {
		LookUp existingCategory = lookUpRepository.findFirstByName(category.getName()).orElse(null);

		if (existingCategory != null) {
			// Update existing category (Avoid inserting duplicate)
			for (LookUp subcategory : category.getChildren()) {
				boolean subcategoryExists = existingCategory.getChildren().stream()
						.anyMatch(existingSub -> existingSub.getName().equals(subcategory.getName()));

				if (!subcategoryExists) {
					subcategory.setType("SUBCATEGORY");
					subcategory.setParent(existingCategory);
					existingCategory.getChildren().add(subcategory);
				}
			}
			return lookUpRepository.save(existingCategory);
		} else {
			// Insert new category
			category.setType("CATEGORY");

			for (LookUp subcategory : category.getChildren()) {
				subcategory.setType("SUBCATEGORY");
				subcategory.setParent(category);
			}

			return lookUpRepository.save(category);
		}
	}

	public Categories updateCategory(int categoryId, Categories updatedCategory) {
		return categoriesRepo.findById(categoryId).map(category -> {
			category.setCategoryName(updatedCategory.getCategoryName());
			category.setStatus(updatedCategory.getStatus());

			return categoriesRepo.save(category);
		}).orElseThrow(() -> new RuntimeException("Category not found"));
	}

	/**
	 * Retrieves a list of categories or subcategories from the `look_up` table.
	 * 
	 * Subclasses that override this method should ensure the query returns the
	 * appropriate data. The response is returned as a map with keys "data" and
	 * "message".
	 * 
	 * @return A map containing the list of category names and a success message.
	 */

	// Get All Categories
	public List<Map<String, Object>> getCategories() {
		List<Object[]> categoriesData = categoriesRepo.findIdAndCategoryName();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] category : categoriesData) {
			Map<String, Object> categoryMap = new HashMap<>();
			categoryMap.put("id", category[0]);
			categoryMap.put("categoryName", category[1]);
			result.add(categoryMap);
		}
		return result;
	}

	public Map<String, Object> deleteCategory(int id) {
		Map<String, Object> response = new HashMap<>();
		Optional<Categories> categoryOptional = categoriesRepo.findById(id);

		if (categoryOptional.isPresent()) {
			Categories category = categoryOptional.get();

			// Mark as deleted (soft delete)
			category.setDeleted(true);
			categoriesRepo.save(category); // Save the updated category

			response.put("message", "Category marked as deleted.");
			response.put("success", true);
		} else {
			response.put("message", "Category not found.");
			response.put("success", false);
		}
		return response;
	}

	public List<Map<String, Object>> getAllCategories() {
		List<LookUp> categories = lookUpRepository.findByType("CATEGORY");

		List<Map<String, Object>> filteredCategories = new ArrayList<>();

		for (LookUp category : categories) {
			Map<String, Object> categoryData = new HashMap<>();
			categoryData.put("id", category.getId());
			categoryData.put("name", category.getName());
			filteredCategories.add(categoryData);
		}

		return filteredCategories;
	}

	public Map<String, Object> updateCategory(int id, String name, String status) {
		return lookUpRepository.findById(id).map(category -> {
			if (name != null && !name.isEmpty()) {
				category.setName(name);
			}
			if (status != null && !status.isEmpty()) {
				category.setStatus(status);
			}

			LookUp updatedCategory = lookUpRepository.save(category);

			// Returning only ID and name in response
			Map<String, Object> response = new HashMap<>();
			response.put("id", updatedCategory.getId());
			response.put("name", updatedCategory.getName());
			response.put("status", updatedCategory.getStatus());

			return response;
		}).orElseThrow(() -> new RuntimeException("Category not found"));
	}

}
