package com.claimit.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.claimit.entity.Categories;
import com.claimit.entity.LookUp;
import com.claimit.service.LookUpService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/lookup")
public class LookUpController {

	private final LookUpService lookUpService;

	private final ObjectMapper objectMapper;

	public LookUpController(LookUpService lookUpService, ObjectMapper objectMapper) {
		this.lookUpService = lookUpService;
		this.objectMapper = objectMapper;
	}

	/**
	 * Adds a new category with associated subcategories. This method allows the
	 * addition of a category with its corresponding subcategories. The request body
	 * should contain the category name and a list of subcategories.
	 * 
	 * @param requestBody A map containing the category name and a list of
	 *                    subcategories.
	 * @return A ResponseEntity with a success message indicating that the data has
	 *         been inserted.
	 */

	@PostMapping("/addCategory")
	public ResponseEntity<Map<String, Object>> addCategoryWithSubcategories(
			@RequestPart("category") String categoryJson,
			@RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

		Categories category = objectMapper.readValue(categoryJson, Categories.class);

		if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
			Map<String, Object> response = new HashMap<>();
			response.put("message", "Category name cannot be empty");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		return lookUpService.addCategoryWithSubcategories(category, image);
	}

	/**
	 * Retrieves categories and their associated subcategories. This method fetches
	 * all available categories along with their respective subcategories, and
	 * returns them in a map format for easy consumption by the client.
	 * 
	 * @return A ResponseEntity containing a map with categories and subcategories.
	 */
	@GetMapping
	public List<Map<String, Object>> getCategories() {
		return lookUpService.getCategories();
	}

	/**
	 * This endpoint updates an existing category in the system. The category is
	 * identified by its id, and the new details are provided in the request body.
	 * 
	 * If the category exists, it updates the fields and returns the updated
	 * category. If the category does not exist, an appropriate error response
	 * should be returned (handled in lookUpService.updateCategory).
	 */

	@PutMapping("/categories/{id}")
	public ResponseEntity<Categories> updateCategory(@PathVariable int id, @RequestBody Categories updatedCategory) {
		return ResponseEntity.ok(lookUpService.updateCategory(id, updatedCategory));
	}

	/**
	 * This endpoint deletes an existing category based on its id.
	 * 
	 * The deletion logic might be a soft delete (marking the category as deleted)
	 * or a hard delete (removing it permanently). The response contains a success
	 * message and relevant details.
	 **/
	
	@DeleteMapping
	public ResponseEntity<Map<String, Object>> deleteCategory(@RequestParam int id) {
		Map<String, Object> response = lookUpService.deleteCategory(id);
		return ResponseEntity.ok(response);
	}

	/** LookUP APIS */

	@GetMapping("/categories")
	public ResponseEntity<List<Map<String, Object>>> getAllCategories() {
		List<Map<String, Object>> categories = lookUpService.getAllCategories();
		return ResponseEntity.ok(categories);
	}

	@PutMapping("/categories/update")
	public ResponseEntity<Map<String, Object>> updateCategory(@RequestParam int id,
			@RequestParam(required = false) String name, @RequestParam(required = false) String status) {

		Map<String, Object> updatedCategory = lookUpService.updateCategory(id, name, status);
		return ResponseEntity.ok(updatedCategory);
	}

	@PostMapping("/add")
	public ResponseEntity<List<LookUp>> addCategoriesWithSubcategories(@RequestBody Object categories) {
		List<LookUp> savedCategories = new ArrayList<>();

		if (categories instanceof List) {
			// Handle multiple category insertion
			for (LookUp category : (List<LookUp>) categories) {
				savedCategories.add(lookUpService.addCategoryWithSubcategories(category));
			}
		} else if (categories instanceof Map) {
			// Handle single category insertion
			LookUp singleCategory = new ObjectMapper().convertValue(categories, LookUp.class);
			savedCategories.add(lookUpService.addCategoryWithSubcategories(singleCategory));
		}

		return ResponseEntity.ok(savedCategories);
	}

}
