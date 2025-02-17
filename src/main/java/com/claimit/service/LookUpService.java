package com.claimit.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.claimit.constants.Constants;
import com.claimit.entity.Categories;
import com.claimit.entity.LookUp;
import com.claimit.entity.Subcategories;
import com.claimit.repo.CategoriesRepo;
import com.claimit.repo.LookUpRepository;

import jakarta.transaction.Transactional;

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

	@Transactional
	public ResponseEntity<Map<String, Object>> addCategoryWithSubcategories(Categories category, MultipartFile image)
			throws IOException {
		Map<String, Object> response = new HashMap<>();

		if (category == null) {
			response.put("message", "Category data cannot be null");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
			response.put("message", "Category name cannot be empty");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		String categoryName = category.getCategoryName().trim().toLowerCase();
		byte[] imageBytes = (image != null && !image.isEmpty()) ? image.getBytes() : null;

		String defaultStatus = "ACTIVE";

		Categories existingCategory = categoriesRepo.findAll().stream()
				.filter(c -> c.getCategoryName() != null && c.getCategoryName().trim().equalsIgnoreCase(categoryName))
				.findFirst().orElse(null);

		if (existingCategory != null) {
			if (existingCategory.isDeleted()) {
				existingCategory.setDeleted(false);
				existingCategory.setStatus(defaultStatus);
				if (imageBytes != null) {
					existingCategory.setImage(imageBytes);
				}
			}

			if (category.getSubcategories() != null) {
				for (Subcategories newSubcategory : category.getSubcategories()) {
					if (newSubcategory.getName() == null || newSubcategory.getName().isBlank()) {
						continue;
					}

					String cleanSubcategoryName = newSubcategory.getName().trim().toLowerCase();

					boolean subcategoryExists = existingCategory.getSubcategories().stream()
							.anyMatch(existingSub -> existingSub.getName() != null
									&& existingSub.getName().trim().equalsIgnoreCase(cleanSubcategoryName));

					if (!subcategoryExists) {
						Subcategories subcategory = new Subcategories();
						subcategory.setName(newSubcategory.getName().trim());
						subcategory.setCategory(existingCategory);
						subcategory.setStatus(defaultStatus);
						existingCategory.getSubcategories().add(subcategory);
					}
				}
			}

			categoriesRepo.save(existingCategory);
			response.put("message", "Successfully added");
			response.put("message", Constants.ALREADY_EXISTS);
//			response.put("categoryName", existingCategory.getCategoryName());
//			return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
			return ResponseEntity.ok(response);

		} else {
			category.setCategoryName(categoryName);
			category.setStatus(defaultStatus);
			category.setImage(imageBytes);
			category.setDeleted(false);

			List<Subcategories> subcategoriesList = new ArrayList<>();

			if (category.getSubcategories() != null) {
				for (Subcategories newSubcategory : category.getSubcategories()) {
					if (newSubcategory.getName() == null || newSubcategory.getName().isBlank()) {
						continue;
					}

					Subcategories subcategory = new Subcategories();
					subcategory.setName(newSubcategory.getName().trim());
					subcategory.setCategory(category);
					subcategory.setStatus(defaultStatus);
					subcategoriesList.add(subcategory);
				}
			}

			category.setSubcategories(subcategoriesList);
			categoriesRepo.save(category);

			response.put("message", "Successfully added");
			response.put("categoryName", category.getCategoryName());
			return ResponseEntity.ok(response);
		}
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

	/**
	 * This method updates an existing category in the database. It retrieves the
	 * category by its ID, updates its name and status, and saves the changes. If
	 * the category does not exist, it throws a RuntimeException.
	 * 
	 * Parameters: categoryId → The unique identifier of the category to be updated.
	 * updatedCategory → The new category details (name and status). If the category
	 * exists, update its categoryName and status and save it in the database. If
	 * the category does not exist, throw a RuntimeException. Returns the updated
	 * Categories object.
	 **/

	public Categories updateCategory(int categoryId, Categories updatedCategory) {
		return categoriesRepo.findById(categoryId).map(category -> {
			category.setCategoryName(updatedCategory.getCategoryName());
			category.setStatus(updatedCategory.getStatus());

			return categoriesRepo.save(category);
		}).orElseThrow(() -> new RuntimeException("Category not found"));
	}

	/**
	 * This method performs a soft delete by marking a category as deleted instead
	 * of removing it permanently from the database. It updates the deleted flag to
	 * true for the specified category.
	 * 
	 * Parameters: id → The unique identifier of the category to be deleted. If the
	 * category exists, set its deleted field to true and save the updated entity in
	 * the database. If the category does not exist, return an error message. A
	 * Map<String, Object> containing a success message and status.
	 ****/

	public Map<String, Object> deleteCategory(int id) {
		Map<String, Object> response = new HashMap<>();
		Optional<Categories> categoryOptional = categoriesRepo.findById(id);

		if (categoryOptional.isPresent()) {
			Categories category = categoryOptional.get();

			category.setDeleted(true);
			categoriesRepo.save(category);

			response.put("message", "Category marked as deleted.");
			response.put("success", true);
		} else {
			response.put("message", "Category not found.");
			response.put("success", false);
		}
		return response;
	}

	////////////////////////////// ** LookUP APIS
	////////////////////////////// *//////////////////////////////////////////

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
}
