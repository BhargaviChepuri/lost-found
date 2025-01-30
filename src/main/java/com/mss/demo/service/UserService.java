
package com.mss.demo.service;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mss.demo.constants.Constants;
import com.mss.demo.dto.ClaimHistoryDTO;
import com.mss.demo.dto.ItemStatus;
import com.mss.demo.dto.ItemsRequestDTO;
import com.mss.demo.dto.ItemsSearchDTO;
import com.mss.demo.entity.Categories;
import com.mss.demo.entity.ClaimHistory;
import com.mss.demo.entity.Items;
import com.mss.demo.entity.ItemsRequest;
import com.mss.demo.entity.ResponseMessage;
import com.mss.demo.entity.User;
import com.mss.demo.exception.ItemNotFoundException;
import com.mss.demo.exception.ResourceNotFoundException;
import com.mss.demo.repo.CategoriesRepo;
import com.mss.demo.repo.ClaimHistoryRepository;
import com.mss.demo.repo.ItemRequestRepo;
import com.mss.demo.repo.ItemsRepo;
import com.mss.demo.repo.UserRepo;

@Service
public class UserService {

	@Autowired
	private ItemsRepo itemsRepository;

	@Autowired
	private ItemRequestRepo itemRequestRepo;

	@Autowired
	private ClaimHistoryRepository claimHistoryRepository;

	@Autowired
	private UserRepo userRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private CategoriesRepo categoriesRepo;


	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);


	/**
	 * Method to save the claim history for an item and a user. This records the
	 * claim status, claim date, and user details into the claim history for
	 * auditing and tracking purposes.
	 *
	 * @param item       the item being claimed
	 * @param user       the user making the claim
	 * @param status     the current status of the claim
	 * @param claimNotes additional notes for the claim
	 */

	private void saveClaimHistory(Items item, User user, ItemStatus status, String claimNotes) {
		LOGGER.info("Saving claim history for item: {} and user: {}", item.getItemId(), user.getUserId());

		ClaimHistory claimHistory = new ClaimHistory();
		claimHistory.setUser(user);
		claimHistory.setClaimStatus(status);
		claimHistory.setClaimDate(LocalDateTime.now());
		claimHistory.setUserEmail(user.getEmail());
		claimHistory.setRequest(itemRequestRepo.findFirstByItem_ItemIdAndStatus(item.getItemId(), status).get());

		claimHistoryRepository.save(claimHistory);
		LOGGER.info("Claim history saved successfully for item: {} and user: {}", item.getItemId(), user.getUserId());

	}


	/**
	 * Method to fetch claim history and item request details based on the user's
	 * email. It retrieves the claim history and item requests for a user identified
	 * by their email address.
	 *
	 * @param email the email of the user for whom to fetch claim history and item
	 *              requests
	 * @return a map containing claim history, item requests, and a success message
	 */
	public Map<String, Object> getClaimHistory() {
		LOGGER.info("Fetching all claim history and item requests.");

		Map<String, Object> response = new HashMap<>();

		List<ClaimHistoryDTO> claimHistory = claimHistoryRepository.findAllClaimHistory();
		response.put(Constants.SUCCESS, true);
		response.put("claimHistory", claimHistory);

		response.put(Constants.MESSAGE,
				!claimHistory.isEmpty() ? "All claim history and item requests retrieved successfully."
						: "No claim history or item requests found.");
		LOGGER.info("Fetched all claim history and item requests.");

		return response;
	}

	public Map<String, Object> getClaimHistoryByEmail(String email, String userName) {
		LOGGER.info("Fetching claim history for email: {}", email);
		System.out.println("Fetching claim history for email: {}");
		Map<String, Object> response = new HashMap<>();

		if (email == null || email.trim().isEmpty()) {
			LOGGER.warn("Email cannot be null or empty.");

			response.put(Constants.SUCCESS, false);
			response.put(Constants.MESSAGE, "Email cannot be null or empty.");
			response.put("claimHistory", List.of());
			response.put("itemRequests", List.of());
			return response;
		}

		Optional<User> optionalUser = userRepository.findByEmailOrUserName(email,userName);
		
		System.out.println("optionalUser " +optionalUser);
		if (optionalUser.isEmpty()) {
			LOGGER.warn("No user found for email: {}", email);

			response.put(Constants.SUCCESS, true);
			response.put("claimHistory", List.of());
			response.put("itemRequests", List.of());
			response.put(Constants.MESSAGE, "No user found for the provided email.");
			return response;
		}

		User user = optionalUser.get();
		LOGGER.info("Found user: {} for email: {}", user.getUserId(), email);

		List<ItemsRequestDTO> itemRequests = itemRequestRepo.findByUserIdAndExcludeStatus(user.getUserId(),
				ItemStatus.CLAIMED);

		List<ClaimHistoryDTO> claimHistory = claimHistoryRepository.findClaimHistoryByEmail(email);

		response.put(Constants.SUCCESS, true);
		response.put("claimHistory", claimHistory);
		response.put("itemRequests", itemRequests);
		response.put(Constants.MESSAGE,
				!claimHistory.isEmpty() || !itemRequests.isEmpty()
						? "Claim history and item requests retrieved successfully."
						: "No claim history or item requests found.");
		LOGGER.info("Fetched claim history and item requests for email: {}", email);

		return response;
	}

	/**
	 * Method to search for items based on a query string. If the query is empty, it
	 * fetches all items; otherwise, it performs a refined search based on item
	 * name, color, and category.
	 *
	 * @param query the search query provided by the user
	 * @return a list of items matching the search criteria
	 */
	public List<ItemsSearchDTO> searchItems(String query) {
		LOGGER.info("Starting item search with query: {}", query);

		if (query == null || query.trim().isEmpty()) {
			LOGGER.info("No query provided, fetching all items.");

			List<ItemsSearchDTO> results = getAllItems();

			if (results.isEmpty()) {
				LOGGER.warn("No items found.");

				throw new ItemNotFoundException("No items found.");
			}
			LOGGER.info("Found {} items.", results.size());

			return results;
		} else {
			LOGGER.info("Cleaning search query: {}", query);

			String cleanedQuery = cleanSearchQuery(query);
			LOGGER.info("Cleaned query: {}", cleanedQuery);

			List<ItemsSearchDTO> results = searchByItemNameColorAndCategory(cleanedQuery);

			if (results.isEmpty()) {
				LOGGER.warn("No items found matching search: {}", cleanedQuery);

				throw new ItemNotFoundException("No items found matching your search");
			}
			LOGGER.info("Found {} items matching the search.", results.size());

			return results;
		}
	}

	/**
	 * Method to clean the search query by removing common stop words. It removes
	 * words like 'I', 'my', 'lost', etc., and returns the cleaned query.
	 *
	 * @param query the search query to be cleaned
	 * @return the cleaned query
	 */
	private String cleanSearchQuery(String query) {
		LOGGER.info("Cleaning search query: {}", query);
		String cleanedQuery = query.toLowerCase()
				.replaceAll("\\b(i|my|like|lost|a|the|or|to|of|for|in|and|find)\\b", "").trim();
		LOGGER.info("Cleaned search query: {}", cleanedQuery);
		return cleanedQuery;
	}

	/**
	 * Method to fetch all items from the repository and convert them into a list of
	 * DTOs. It includes item details such as category, color, status, and user
	 * details.
	 *
	 * @return a list of all items as DTOs
	 */
	private List<ItemsSearchDTO> getAllItems() {
		LOGGER.info("Fetching all items from the repository.");

		return itemsRepository.findAll().stream().map(item -> {
			String categoryName = getCategoryNameById(item.getCategoryId());
			return new ItemsSearchDTO(item.getItemId(), item.getReceivedDate(), item.getExpirationDate(),
					item.getColour(), item.getDetectedText(), item.getOrgId(), item.getDescription(), item.getTitle(),
					item.getItemName(), item.getStatus(), item.getUser() != null ? item.getUser().getUserId() : null,
					item.getImage(), item.getUser() != null ? item.getUser().getName() : null,
					item.getUser() != null ? item.getUser().getEmail() : null, categoryName);
		}).collect(Collectors.toList());
	}

	/**
	 * Method to search for items by item name, color, and category based on a
	 * cleaned query. It processes each term in the query to check if it corresponds
	 * to an item name, color, or category.
	 *
	 * @param query the cleaned search query
	 * @return a list of items matching the query by name, color, and/or category
	 */
	private List<ItemsSearchDTO> searchByItemNameColorAndCategory(String query) {
		LOGGER.info("Searching by item name, color, and category with cleaned query: {}", query);

		String[] queryTerms = query.split(" ");
		StringBuilder categoryQueryBuilder = new StringBuilder();
		StringBuilder itemNameQueryBuilder = new StringBuilder();
		String colorQuery = null;

		for (String term : queryTerms) {
			LOGGER.debug("Processing term: {}", term);

			if (isColor(term)) {
				colorQuery = term;
				LOGGER.debug("Identified color: {}", colorQuery);

			} else {
				if (categoryQueryBuilder.length() > 0) {
					categoryQueryBuilder.append(" ");
				}
				categoryQueryBuilder.append(term);

				String potentialCategoryQuery = categoryQueryBuilder.toString().trim();
				if (isCategory(potentialCategoryQuery)) {
					LOGGER.info("Category identified: " + potentialCategoryQuery);
					return itemsRepository.searchByItemNameColorAndCategory(null, colorQuery, potentialCategoryQuery);
				} else {
					itemNameQueryBuilder.append(term).append(" ");
				}
			}
		}

		String itemNameQuery = itemNameQueryBuilder.toString().trim();
		return itemsRepository.searchByItemNameColorAndCategory(itemNameQuery, colorQuery, null);
	}

	/**
	 * Method to check if a term corresponds to a valid category. It verifies if the
	 * term exists as a category in the repository.
	 *
	 * @param term the term to be checked
	 * @return true if the term exists as a category, false otherwise
	 */
	private boolean isCategory(String term) {
		LOGGER.info("Checking if category exists: " + term);
		boolean exists = categoriesRepo.existsByCategoryNameIgnoreCase(term);
		LOGGER.info("Category exists: " + exists);
		return exists;
	}

	/**
	 * Method to check if a term corresponds to a valid color. It attempts to map
	 * the term to a color field from the Color class.
	 *
	 * @param term the term to be checked
	 * @return true if the term corresponds to a valid color, false otherwise
	 */
	private boolean isColor(String term) {
		try {
			Color color = (Color) Color.class.getField(term.toLowerCase()).get(null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Method to fetch the category name by category ID. It retrieves the category
	 * name associated with the given category ID from the repository.
	 *
	 * @param categoryId the ID of the category
	 * @return the name of the category, or 'Unknown Category' if not found
	 */
	private String getCategoryNameById(int categoryId) {
		LOGGER.info("Fetching category name for categoryId: {}", categoryId);

		Categories category = categoriesRepo.findById(categoryId).orElse(null);
		if (category != null) {
			LOGGER.info("Found category: {}", category.getCategoryName());
			return category.getCategoryName();
		} else {
			LOGGER.warn("Category not found for categoryId: {}", categoryId);
			return "Unknown Category";
		}
	}

	/**
	 * Method to upload an image for search purposes. It validates the uploaded
	 * file, checks its type, and attempts to find matching items based on the
	 * uploaded image.
	 *
	 * @param file the image file to be uploaded
	 * @return a map containing the matched items and the success message
	 * @throws IOException if an error occurs during file processing
	 */
	public Map<String, Object> uploadImageForSearch(MultipartFile file) throws IOException {
		LOGGER.info("Starting image upload for search.");

		Map<String, Object> response = new HashMap<>();
		response.put(Constants.SUCCESS, false);
		response.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		if (file.isEmpty()) {
			LOGGER.error("File is empty.");

			throw new IllegalArgumentException("File cannot be empty.");
		}

		String fileName = file.getOriginalFilename();
		if (fileName == null || !fileName.matches("(?i).+\\.(jpg|jpeg|png|gif|bmp|jfif)$")) {
			LOGGER.error("Invalid file type: {}", fileName);

			throw new IllegalArgumentException("Only image files are supported.");
		}
		LOGGER.info("File is valid: {}", fileName);

		byte[] uploadedImageBytes = file.getBytes();
		LOGGER.info("Fetching all items with images for comparison.");

		List<Items> allItemsWithImages = itemsRepository.findAll().stream().filter(item -> item.getImage() != null)
				.collect(Collectors.toList());
		LOGGER.info("Finding matching items based on uploaded image.");

		List<Map<String, Object>> matchedItems = allItemsWithImages.stream().filter(item -> {
			byte[] itemImageBytes = item.getImage();
			return areImagesSimilar(uploadedImageBytes, itemImageBytes);
		}).map(item -> {
			Map<String, Object> itemDetails = new HashMap<>();
			itemDetails.put("itemName", item.getItemName());
			itemDetails.put("dominantColor", item.getColour());
			itemDetails.put("detectedText", item.getDetectedText());
			itemDetails.put("image", item.getImage());
			itemDetails.put("categoryName", getCategoryNameById(item.getCategoryId()));
			itemDetails.put("status", item.getStatus()); // Ensure 'status' is a property of 'Items'
			itemDetails.put("receivedDate", item.getReceivedDate());
			itemDetails.put("title", item.getTitle());

			if (item.getUser() != null) {
				itemDetails.put("name", item.getUser().getName());
				itemDetails.put("email", item.getUser().getEmail());
				itemDetails.put("status", item.getStatus());
				itemDetails.put("receivedDate", item.getReceivedDate());
			}
			return itemDetails;
		}).collect(Collectors.toList());

		if (!matchedItems.isEmpty()) {
			LOGGER.info("Found {} matching items.", matchedItems.size());

			response.put("matchedItems", matchedItems);
			response.put(Constants.SUCCESS, true);
			response.put(Constants.MESSAGE, "Matching items found.");
		} else {
			LOGGER.info("No matching items found.");

			response.put(Constants.MESSAGE, "No matching items found.");
		}

		return response;
	}

	/**
	 * Method to compare two images for similarity. This uses basic byte comparison.
	 * For better accuracy, use advanced techniques like pHash.
	 */
	private boolean areImagesSimilar(byte[] image1, byte[] image2) {
		LOGGER.info("Starting image similarity comparison.");
		if (image1 == null || image2 == null || image1.length != image2.length) {
			LOGGER.warn("Images are not similar: One or both images are null or have different sizes.");

			return false;
		}

		for (int i = 0; i < image1.length; i++) {
			if (image1[i] != image2[i]) {
				LOGGER.warn("Images are not similar: Byte mismatch at position {}", i);

				return false;
			}
		}
		LOGGER.info("Images are similar.");

		return true;
	}

	/**
	 * Updates the status of a claim and sends a notification to both the user and
	 * admin. 1. Retrieves the claim history by the provided claimId. 2. Checks if
	 * the claim exists in the repository; if not, it throws a
	 * ResourceNotFoundException. 3. Logs the old claim status. 4. Updates the claim
	 * status to the new one provided in the method argument. 5. Saves the updated
	 * claim status back to the repository. 6. Sends a notification to both the user
	 * and admin about the status change. 7. Returns a response map containing the
	 * updated status and a message.
	 *
	 * @param claimId The ID of the claim whose status is to be updated.
	 * @param status  The new status to set for the claim.
	 * @return A map containing the updated claim status and a success message.
	 * @throws ResourceNotFoundException if the claim with the given claimId is not
	 *                                   found.
	 */
	public Map<String, Object> updateClaimStatusAndNotify(int claimId, ItemStatus status) {
		LOGGER.info("Starting to update claim status for claimId: {}", claimId);

		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);
		ClaimHistory claimHistory = claimHistoryRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("ClaimHistory not found"));
		ItemStatus oldStatus = claimHistory.getClaimStatus();
		LOGGER.info("Old status: {}", oldStatus);

		claimHistory.setClaimStatus(status);
		claimHistoryRepository.save(claimHistory);
		LOGGER.info("Claim status updated to: {}", status);

		notificationService.handleStatusChangeNotification(claimHistory);
		LOGGER.info("Notification sent for status change of claimId: {}", claimId);

		Map<String, Object> response = new HashMap<>();
		response.put("Status", status);
		response.put("message", "Claim status updated and notification sent to user and admin.");
		LOGGER.info("Response generated: {}", response);

		return response;
	}

	/**
	 * Calculates the statistics for each item category in terms of total weight and
	 * item count.
	 *
	 * 1. Initializes maps to store the total weight and item count for each
	 * category. 2. Fetches all categories from the repository. 3. Initializes
	 * category data with zero values for weight and item count. 4. Retrieves all
	 * unclaimed items and processes their category and weight data. 5. Aggregates
	 * the total weight and item count for each category. 6. Formats the results
	 * into a list of maps containing category name, total weight, and total item
	 * count. 7. Returns a response map with the calculated category statistics.
	 *
	 * @return A map containing the calculated category statistics.
	 */
	public Map<String, Object> calculateCategoryStats() {
		LOGGER.info("Starting category stats calculation.");

		Map<String, Object> response = new HashMap<>();
		Map<String, Double> categoryWeights = new HashMap<>();
		Map<String, Integer> categoryItemCount = new HashMap<>();

		List<Categories> allCategories = categoriesRepo.findAll();
		LOGGER.info("Found {} categories.", allCategories.size());

		for (Categories category : allCategories) {
			String categoryName = category.getCategoryName();
			categoryWeights.put(categoryName, 0.0);
			categoryItemCount.put(categoryName, 0);
		}

		List<Items> unclaimedItems = itemsRepository.findByStatus(ItemStatus.UNCLAIMED);
		LOGGER.info("Found {} unclaimed items.", unclaimedItems.size());

		for (Items item : unclaimedItems) {
			int categoryId = item.getCategoryId();
			Categories category = categoriesRepo.findById(categoryId).orElse(null);

			if (category != null) {
				Double weight = 0.0;
				try {
					weight = Double.parseDouble(item.getCarbonWeight().replace(" kg", "").trim());
				} catch (NumberFormatException e) {
					weight = 0.0;
				}

				String categoryName = category.getCategoryName();
				categoryWeights.put(categoryName, categoryWeights.getOrDefault(categoryName, 0.0) + weight);
				categoryItemCount.put(categoryName, categoryItemCount.getOrDefault(categoryName, 0) + 1);
			}
		}

		List<Map<String, String>> categories = new ArrayList<>();
		for (String categoryName : categoryWeights.keySet()) {
			Map<String, String> categoryStats = new HashMap<>();
			categoryStats.put("category", categoryName);
			categoryStats.put("totalWeight", String.format("%.2f kg", categoryWeights.get(categoryName)));
			categoryStats.put("totalItems", String.valueOf(categoryItemCount.get(categoryName)));
			categories.add(categoryStats);
		}

		response.put("categories", categories);
		LOGGER.info("Category stats calculated: {}", categories);

		return response;
	}


}
