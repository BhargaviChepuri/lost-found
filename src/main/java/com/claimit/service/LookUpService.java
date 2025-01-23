package com.claimit.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.claimit.constants.Constants;

import jakarta.persistence.EntityManager;

@Service
public class LookUpService {

	@Autowired
	private EntityManager entityManager;

	@Transactional
	public void saveCategoryAndSubcategories(String categoryName, List<String> subcategories) {
		String insertCategorySql = "INSERT INTO look_up (code, name, status) VALUES (:code, :name, :status)";
		entityManager.createNativeQuery(insertCategorySql).setParameter("code", "CAT")
				.setParameter("name", categoryName).setParameter("status", true).executeUpdate();

		String insertSubcategorySql = "INSERT INTO look_up (code, name, status) VALUES (:code, :name, :status)";
		for (String subcategory : subcategories) {
			entityManager.createNativeQuery(insertSubcategorySql).setParameter("code", "SCT")
					.setParameter("name", subcategory).setParameter("status", true).executeUpdate();
		}
	}

	public Map<String, Object> getCategoriesOrSubcategories() {
		String query = "SELECT name FROM look_up WHERE code = 'C'";
		List<String> result = entityManager.createNativeQuery(query).getResultList();

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("data", result);
		response.put("message", "Successfully retrieved data.");
		return response;
	}

}
