package com.claimit.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.claimit.entity.Categories;
import com.claimit.entity.LookUp;
import com.claimit.service.LookUpService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/lookup")
public class LookUpController {

	private final LookUpService lookUpService;

	public LookUpController(LookUpService lookUpService) {
		this.lookUpService = lookUpService;
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
//	@PostMapping("/add")
//	public ResponseEntity<List<LookUp>> addCategoriesWithSubcategories(@RequestBody List<LookUp> categories) {
//		List<LookUp> savedCategories = new ArrayList<>();
//
//		for (LookUp category : categories) {
//			savedCategories.add(lookUpService.addCategoryWithSubcategories(category));
//		}
//
//		return ResponseEntity.ok(savedCategories);
//	}

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

	@PutMapping("/categories/{id}")
	public ResponseEntity<Categories> updateCategory(@PathVariable int id, @RequestBody Categories updatedCategory) {
		return ResponseEntity.ok(lookUpService.updateCategory(id, updatedCategory));
	}

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

}
