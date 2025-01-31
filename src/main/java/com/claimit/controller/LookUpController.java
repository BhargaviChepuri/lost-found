package com.claimit.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.claimit.service.LookUpService;

@RestController
@RequestMapping("/lookup")
public class LookUpController {

	@Autowired
	public LookUpService lookUpService;

	@PostMapping
	public ResponseEntity<String> addCategoryWithSubcategories(@RequestBody Map<String, Object> requestBody) {
		String categoryName = (String) requestBody.get("categoryName");
		List<String> subcategories = (List<String>) requestBody.get("subcategories");

		lookUpService.saveCategoryAndSubcategories(categoryName, subcategories);
		return ResponseEntity.ok("Data inserted successfully.");
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> getCategoriesOrSubcategories() {
		Map<String, Object> response = lookUpService.getCategoriesOrSubcategories();
		return ResponseEntity.ok(response);
	}

}
