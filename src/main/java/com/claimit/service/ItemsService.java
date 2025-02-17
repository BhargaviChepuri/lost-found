package com.claimit.service;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.claimit.constants.Constants;
import com.claimit.dto.ItemDTO;
import com.claimit.dto.ItemStatus;
import com.claimit.dto.ItemStatusCountDTO;
import com.claimit.dto.ItemsSearchDTO;
import com.claimit.entity.Categories;
import com.claimit.entity.Items;
import com.claimit.entity.ItemsRequest;
import com.claimit.entity.Organisation;
import com.claimit.entity.Subcategories;
import com.claimit.exception.ItemNotFoundException;
import com.claimit.repo.CategoriesRepo;
import com.claimit.repo.ItemRequestRepo;
import com.claimit.repo.ItemsRepo;
import com.claimit.repo.OrganisationRepository;
import com.claimit.repo.SubCategoriesRepo;
import com.claimit.repo.UserRepo;
import com.claimit.utils.ColorUtils;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.DominantColorsAnnotation;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

@Service
public class ItemsService {

	@Autowired
	private ItemsRepo itemsRepository;

	@Autowired
	private UserRepo userRepository;

	@Autowired
	private ItemRequestRepo itemRequestRepo;

	@Autowired
	private EmailService emailService;

	@Autowired
	private OrganisationRepository organisationRepository;

	@Autowired
	private CategoriesRepo categoriesRepo;

	@Autowired
	private SubCategoriesRepo subCategoriesRepo;

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemsService.class);

	/**
	 * Detects labels from an image and updates item information based on detected
	 * features.
	 *
	 * 1. Reads image bytes from the provided file. 2. Creates an image object and
	 * sets features for label detection, object localization, and text detection.
	 * 3. Makes a request to Google Cloud Vision API to detect labels, objects, and
	 * text in the image. 4. Processes the response to extract labels and other
	 * properties like dominant color and detected text. 5. Creates or retrieves the
	 * organization associated with the item. 6. Matches the detected labels with
	 * subcategories and assigns appropriate category and subcategory. 7. Calculates
	 * carbon weight based on detected labels. 8. Saves the item with relevant
	 * information such as name, description, detected labels, and carbon weight.
	 * 
	 * @param file  The image file to be analyzed.
	 * @param orgId The organization ID associated with the item.
	 * @return A map containing the status of the operation, including success or
	 *         failure and item details.
	 * 
	 *         Calculates the carbon weight for an item based on its detected
	 *         labels.
	 */

	public Map<String, Object> detectLabels(MultipartFile file, String orgId, List<String> editedLabels)
			throws IOException {

		LOGGER.info("Starting label detection for organization ID: {}", orgId);

		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		ByteString imgBytes = ByteString.readFrom(file.getInputStream());
		Image img = Image.newBuilder().setContent(imgBytes).build();

		List<Feature> features = List.of(Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build(),
				Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build(),
				Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).build(),
				Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build());

		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().setImage(img).addAllFeatures(features).build();

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			AnnotateImageResponse response = client.batchAnnotateImages(List.of(request)).getResponsesList().get(0);
			LOGGER.info("Image analysis completed for organization ID: {}", orgId);

			Items item = new Items();
			item.setImage(file.getBytes());

			Date setReceivedDate = new Date();
			item.setReceivedDate(setReceivedDate);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(setReceivedDate);
			calendar.add(Calendar.DAY_OF_MONTH, 30);
			Date expirationDate = calendar.getTime();
			item.setExpirationDate(expirationDate);

			Organisation organisation = organisationRepository.findById(orgId).orElseGet(() -> {
				Organisation newOrganisation = new Organisation();
				newOrganisation.setOrgId(orgId);
				newOrganisation.setOrgName("Default Organisation " + orgId);
				LOGGER.info("Created new organization with ID: {}", orgId);
				return organisationRepository.save(newOrganisation);
			});

			List<String> finalLabels = (editedLabels != null && !editedLabels.isEmpty()) ? editedLabels
					: response.getLabelAnnotationsList().stream().map(EntityAnnotation::getDescription).distinct()
							.collect(Collectors.toList());

			String title = !finalLabels.isEmpty() ? finalLabels.get(0) : "Unknown Item";
			String description = "Detected Labels: " + String.join(", ", finalLabels);

			List<Subcategories> matchedSubcategories = finalLabels.stream()
					.flatMap(label -> subCategoriesRepo.findByNameLikeIgnoreCase(label.toLowerCase()).stream())
					.distinct().collect(Collectors.toList());

			if (!matchedSubcategories.isEmpty()) {
				Subcategories matchedSubcategory = matchedSubcategories.get(0);
				item.setSubcatgeoryId(matchedSubcategory.getId());
				item.setCategoryId(matchedSubcategory.getCategoryId());
				LOGGER.info("Matched subcategory ID: {} for detected labels", matchedSubcategory.getId());
			} else {
//				item.setSubcatgeoryId(30);
//				item.setCategoryId(7);
				LOGGER.info("No matching subcategories found, setting default category and subcategory.");
			}

			String itemName = finalLabels.isEmpty() ? "Unknown Item"
					: String.join(", ", finalLabels.subList(0, Math.min(3, finalLabels.size())));
			item.setItemName(itemName);

			if (response.hasImagePropertiesAnnotation()) {
				DominantColorsAnnotation colors = response.getImagePropertiesAnnotation().getDominantColors();
				String mostDominantColor = colors.getColorsList().stream()
						.max((colorInfo1, colorInfo2) -> Float.compare(colorInfo1.getScore(), colorInfo2.getScore()))
						.map(colorInfo -> ColorUtils.getClosestColorName((int) colorInfo.getColor().getRed(),
								(int) colorInfo.getColor().getGreen(), (int) colorInfo.getColor().getBlue()))
						.orElse("Unknown");
				item.setColour(mostDominantColor);
				description += ", Dominant Color: " + mostDominantColor;
				LOGGER.info("Detected dominant color: {}", mostDominantColor);
			}

			List<String> texts = response.getTextAnnotationsList().stream().map(EntityAnnotation::getDescription)
					.collect(Collectors.toList());
			String detectedText = texts.isEmpty() ? "None" : String.join(", ", texts);
			item.setDetectedText(detectedText);
			description += ", Text Detected: " + detectedText;

			LocalDate currentDate = LocalDate.now();
			String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

			int latestNumber = itemsRepository.findLatestNumberByDate(formattedDate).orElse(0);
			int newNumber = latestNumber + 1;

			String uniqueId = formattedDate + "-" + newNumber;

			String carbonWeight = calculateCarbonWeight(finalLabels, item.getCategoryId());
			item.setCarbonWeight(carbonWeight);
			item.setTitle(title);
			item.setOrgId(orgId);
			item.setStatus(ItemStatus.UNCLAIMED);
			item.setUniqueId(uniqueId);
			item.setDescription(description);
			itemsRepository.save(item);

			res.put("title", title);
			res.put("description", description);
			res.put("success", true);
			res.put(Constants.MESSAGE, Constants.SUCESSFULLY_ADDED);
			LOGGER.info("Item successfully saved with title: {}", title);

		} catch (Exception e) {
			LOGGER.error("Error occurred during label detection: {}", e.getMessage(), e);

			res.put("exception", e.getMessage());
		}

		return res;
	}

	private String calculateCarbonWeight(List<String> detectedLabels, int categoryId) {
		Map<String, String> carbonWeightMapping = Map.of("plastic", "3.5 kg", "paper", "1.2 kg", "electronics",
				"5.0 kg", "unknown", "0.5 kg");

		for (String label : detectedLabels) {
			if (carbonWeightMapping.containsKey(label.toLowerCase())) {
				return carbonWeightMapping.get(label.toLowerCase());
			}
		}

		return "0.0 kg";
	}

	/**
	 * Uses Google Vision API to extract labels, objects, text, and colors from
	 * images. Associates images with an organization in the database. Matches
	 * detected labels with subcategories in the database. Determines dominant color
	 * and detected text for better classification. Calculates an expiration date
	 * (30 days from upload). Provides a structured JSON response for frontend
	 * display.
	 */

	public Map<String, Object> uploadImageForPreview(MultipartFile file, String orgId) throws IOException {

		LOGGER.info("Starting label detection for organization ID: {}", orgId);

		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		ByteString imgBytes = ByteString.readFrom(file.getInputStream());
		Image img = Image.newBuilder().setContent(imgBytes).build();

		List<Feature> features = List.of(Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build(),
				Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build(),
				Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).build(),
				Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build());

		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().setImage(img).addAllFeatures(features).build();

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			AnnotateImageResponse response = client.batchAnnotateImages(List.of(request)).getResponsesList().get(0);
			LOGGER.info("Image analysis completed for organization ID: {}", orgId);

			Date setReceivedDate = new Date();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(setReceivedDate);
			calendar.add(Calendar.DAY_OF_MONTH, 30);
			Date expirationDate = calendar.getTime();

			Organisation organisation = organisationRepository.findById(orgId).orElseGet(() -> {
				Organisation newOrganisation = new Organisation();
				newOrganisation.setOrgId(orgId);
				newOrganisation.setOrgName("Default Organisation " + orgId);
				LOGGER.info("Created new organization with ID: {}", orgId);

				return organisationRepository.save(newOrganisation);
			});

			List<String> detectedLabels = response.getLabelAnnotationsList().stream()
					.map(EntityAnnotation::getDescription).distinct().collect(Collectors.toList());

			String title = !detectedLabels.isEmpty() ? detectedLabels.get(0) : "Unknown Item";
			String description = "Detected Labels: " + String.join(", ", detectedLabels);

			List<Subcategories> matchedSubcategories = detectedLabels.stream()
					.flatMap(label -> subCategoriesRepo.findByNameLikeIgnoreCase(label.toLowerCase()).stream())
					.distinct().collect(Collectors.toList());

			if (!matchedSubcategories.isEmpty()) {
				Subcategories matchedSubcategory = matchedSubcategories.get(0);
				LOGGER.info("Matched subcategory ID: {} for detected labels", matchedSubcategory.getId());

			} else {
				LOGGER.info("No matching subcategories found, setting default category and subcategory.");

			}

			String itemName = detectedLabels.isEmpty() ? "Unknown Item"
					: String.join(", ", detectedLabels.subList(0, Math.min(3, detectedLabels.size())));

			if (response.hasImagePropertiesAnnotation()) {
				DominantColorsAnnotation colors = response.getImagePropertiesAnnotation().getDominantColors();
				String mostDominantColor = colors.getColorsList().stream()
						.max((colorInfo1, colorInfo2) -> Float.compare(colorInfo1.getScore(), colorInfo2.getScore()))
						.map(colorInfo -> ColorUtils.getClosestColorName((int) colorInfo.getColor().getRed(),
								(int) colorInfo.getColor().getGreen(), (int) colorInfo.getColor().getBlue()))
						.orElse("Unknown");
				description += ", Dominant Color: " + mostDominantColor;
				res.put("colour", mostDominantColor);
				LOGGER.info("Detected dominant color: {}", mostDominantColor);

			}
			List<String> texts = response.getTextAnnotationsList().stream().map(EntityAnnotation::getDescription)
					.collect(Collectors.toList());
			String detectedText = texts.isEmpty() ? "None" : String.join(", ", texts);
			description += ", Text Detected: " + detectedText;

			res.put("expirationDate", expirationDate);
			res.put("receivedDate", setReceivedDate);
			res.put("title", title);
			res.put("description", description);
			res.put("success", true);
			res.put(Constants.MESSAGE, Constants.SUCESSFULLY_ADDED);
			LOGGER.info("Item successfully saved with title: {}", title);

		} catch (Exception e) {
			LOGGER.error("Error occurred during label detection: {}", e.getMessage(), e);

			res.put("exception", e.getMessage());
		}
		return res;
	}

	/**
	 * Retrieves all items from the database and returns them in a response map.
	 * 
	 * This method: 1. Attempts to fetch a list of item summaries from the
	 * repository. 2. If the list is empty or null, it returns a response indicating
	 * no data is found. 3. If items are found, it returns a success response with
	 * the item data. 4. In case of an exception, it returns a failure response
	 * indicating an error.
	 * 
	 * @return A map containing the success status, a message, and the item data (if
	 *         available).
	 */
	public Map<String, Object> getAllItems() {
		LOGGER.info("Entering getAllItems method.");
		Map<String, Object> response = new HashMap<>();
		try {
			List<ItemDTO> items = itemsRepository.findItemsSummary();
			if (items == null || items.isEmpty()) {
				response.put(Constants.SUCCESS, false);
				response.put(Constants.MESSAGE, Constants.NODATA);
			} else {
				response.put(Constants.SUCCESS, true);
				response.put(Constants.MESSAGE, Constants.SUCESSFULLY_RETRIVED_DATA);
				response.put(Constants.DATA, items);
			}
		} catch (Exception e) {
			response.put(Constants.SUCCESS, false);
			response.put(Constants.MESSAGE, Constants.SOMETHING_WENT_WRONG);
		}
		return response;
	}

	/**
	 * Archives expired items based on the received date and sends notifications.
	 * 
	 * This method: 1. Accepts an optional item ID to archive a specific item or
	 * process all items. 2. Calculates the expiration date (90 days from the
	 * received date). 3. Sends expiration reminder emails to users if the item is
	 * about to expire (30 or 3 days before expiration). 4. Archives expired items
	 * (if the current date is past the expiration date) and sends archived
	 * notifications to the admin and user.
	 * 
	 * @param itemId The ID of the item to be archived (optional). If null, all
	 *               items will be processed.
	 * @return A response map indicating success or failure, including the relevant
	 *         message.
	 */
	// Archive Expired Items
	@Scheduled(fixedRate = 86400000) // Runs every 24 hours
	public void archiveExpiredItemsAutomatically() {
		Date currentDate = new Date();

		List<Items> items = itemsRepository.findByStatus(ItemStatus.UNCLAIMED);

		for (Items item : items) {
			Date expirationDate = item.getExpirationDate();

			if (expirationDate != null && expirationDate.before(currentDate)) {
				item.setStatus(ItemStatus.ARCHIVED);
				itemsRepository.save(item);
			}
		}
	}

	public Map<String, Object> archiveExpiredItems(String fromDateStr, String toDateStr, String expirationDateStr) {
		LOGGER.info("Entering archiveExpiredItems with fromDate: {} to toDate: {} and expirationDate: {}", fromDateStr,
				toDateStr, expirationDateStr);

		Map<String, Object> res = new HashMap<>();

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fromDate = sdf.parse(fromDateStr);
			Date toDate = sdf.parse(toDateStr);
			Date newExpirationDate = null;

			if (expirationDateStr != null && !expirationDateStr.isEmpty()) {
				newExpirationDate = sdf.parse(expirationDateStr);
			}

			List<Items> archivedItems = itemsRepository.findByStatusAndReceivedDateBetween(ItemStatus.ARCHIVED,
					fromDate, toDate);

			for (Items item : archivedItems) {
				item.setStatus(ItemStatus.UNCLAIMED);

				if (newExpirationDate != null) {
					item.setExpirationDate(newExpirationDate);
				}

				item.setReceivedDate(new Date());

				itemsRepository.save(item);
			}

		} catch (Exception e) {
			LOGGER.error("Error processing archived items", e);
			res.put(Constants.MESSAGE, "Failed to process archived items.");
			res.put(Constants.SUCCESS, false);
			return res;
		}

		res.put(Constants.MESSAGE, Constants.SUCESSFULLY_UPDATED);
		res.put(Constants.SUCCESS, true);
		return res;
	}

	/**
	 * Approves or rejects an item request based on the given status. 1. Validates
	 * the status provided for the request. 2. Checks if there is an existing
	 * request for the item that is pending approval. 3. If the request exists,
	 * updates its status to either approved or rejected. 4. If rejected, a
	 * rejection reason must be provided. 5. Saves the updated request and item
	 * status, then returns a response indicating success or failure.
	 * 
	 * @param itemId         The ID of the item being claimed.
	 * @param status         The new status for the item (approved, rejected, etc.).
	 * @param rejectedReason The reason for rejection (required if the status is
	 *                       rejected).
	 * @return A map containing the result of the approval/rejection process,
	 *         including a success message and HTTP status.
	 */
	public Map<String, Object> approveOrRejectRequest(int itemId, ItemStatus status, String rejectedReason) {
		Map<String, Object> response = new HashMap<>();

		if (status == null) {
			LOGGER.warn("Received null status for itemId: {}", itemId);

			response.put(Constants.MESSAGE, "Status cannot be null.");
			response.put("status", HttpStatus.BAD_REQUEST);
			return response;
		}
		LOGGER.info(" approval/rejection process for Item ID: {}", itemId);

		Optional<ItemsRequest> requestOpt = itemRequestRepo.findFirstByItemItemIdAndStatus(itemId,
				ItemStatus.PENDING_APPROVAL);
		Optional<Items> items = itemsRepository.findById(itemId);
		items.get().setStatus(status);
		LOGGER.info("Updating item status to: {}", status);

		if (requestOpt.isEmpty()) {
			LOGGER.warn("Status already updated for Item ID: {}", itemId);

			response.put(Constants.MESSAGE, "Already status was updated for Item ID: " + itemId);
			response.put("status", status);
			return response;
		}

		ItemsRequest itemsRequest = requestOpt.get();

		if (status == ItemStatus.REJECTED) {
			if (rejectedReason == null || rejectedReason.trim().isEmpty()) {
				LOGGER.warn("Rejection reason is missing for Item ID: {} with REJECTED status", itemId);

				response.put(Constants.MESSAGE, "Rejection reason is required for status REJECTED.");
				response.put("status", HttpStatus.BAD_REQUEST);
				return response;
			}

			itemsRequest.setRejectedReason(rejectedReason);
			LOGGER.info("Rejection reason provided for Item ID: {}: {}", itemId, rejectedReason);

		}

		itemsRequest.setClaimedDate(LocalDateTime.now());
		itemsRequest.setStatus(status);
		LOGGER.info("Saving updated request for Item ID: {}", itemId);

		itemRequestRepo.save(itemsRequest);

		items.get().setStatus(status);
		itemsRepository.save(items.get());
		LOGGER.info("Item ID: {} status updated to: {}", itemId, status);

		response.put(Constants.MESSAGE, Constants.SUCESSFULLY_UPDATED);
		response.put("status", HttpStatus.OK);
		LOGGER.info("Approval/Rejection process Successfully successfully for Item ID: {}", itemId);

		return response;
	}

	/**
	 * Retrieves the count of items grouped by status for a specific month.
	 * 
	 * This method queries the database to fetch the count of items grouped by their
	 * status (e.g., unclaimed, pending approval, claimed, rejected, etc.) for the
	 * given month. It returns a list of counts for each item status.
	 * 
	 * @param month The month for which the item status counts are being fetched.
	 * @return A list of `ItemStatusCountDTO` objects, each representing the count
	 *         of items in different statuses for the specified month.
	 */
	public List<ItemStatusCountDTO> getItemStatusCounts(String month) {
		LOGGER.info("Fetching item status counts for month: {}", month);

		List<Object[]> results = itemsRepository.findItemsGroupedByMonth(month);

		List<ItemStatusCountDTO> itemStatusCounts = new ArrayList<>();

		for (Object[] result : results) {
			String monthResult = (String) result[0];
			int totalItems = ((Long) result[1]).intValue();
			int unclaimed = ((Long) result[2]).intValue();
			int pendingApproval = ((Long) result[3]).intValue();
			int pendingPickup = ((Long) result[4]).intValue();
			int claimed = ((Long) result[5]).intValue();
			int rejected = ((Long) result[6]).intValue();
			int archived = ((Long) result[7]).intValue();

			String monthName = monthResult;
			// Log the extracted result values for debugging purposes
			LOGGER.debug(
					"Processed data for month: {} with values - Total Items: {}, Unclaimed: {}, Pending Approval: {}, Pending Pickup: {}, Claimed: {}, Rejected: {}, Archived: {}",
					monthName, totalItems, unclaimed, pendingApproval, pendingPickup, claimed, rejected, archived);

			ItemStatusCountDTO dto = new ItemStatusCountDTO();
			dto.setMonth(monthName);
			dto.setTotalItems(totalItems);
			dto.setUnclaimed(unclaimed);
			dto.setPendingApproval(pendingApproval);
			dto.setPendingPickup(pendingPickup);
			dto.setClaimed(claimed);
			dto.setRejected(rejected);
			dto.setArchived(archived);

			itemStatusCounts.add(dto);
		}
		LOGGER.info("Successfully fetching item status counts for month: {}", month);

		return itemStatusCounts;
	}

	/**
	 * Retrieves the count of items grouped by category for each month.
	 * 
	 * This method queries the database to fetch the count of items in each
	 * category, grouped by month. It then formats the results to return a list of
	 * maps containing the month, category, and the corresponding item count. The
	 * month is represented by its name (e.g., January, February, etc.), and each
	 * category's item count is retrieved and included in the response.
	 * 
	 * @return A list of maps where each map represents a month with its associated
	 *         categories and item counts.
	 */
	public List<Map<String, Object>> getItemCountByCategory() {
		LOGGER.info(" fetch item counts grouped by category for each month.");
		List<Categories> allCategories = categoriesRepo.findAll();
		LOGGER.debug("Fetched all categories: {}", allCategories.size());

		List<Object[]> results = itemsRepository.findItemCountGroupedByCategoryIdAndMonth();

		Map<String, Map<Integer, Integer>> monthCategoryCounts = new HashMap<>();

		for (Object[] result : results) {
			String month = getMonthName((Integer) result[0]);
			Integer categoryId = (Integer) result[1];
			int totalItems = ((Long) result[2]).intValue();

			monthCategoryCounts.putIfAbsent(month, new HashMap<>());

			monthCategoryCounts.get(month).put(categoryId, totalItems);
		}

		List<Map<String, Object>> response = new ArrayList<>();

		for (Map.Entry<String, Map<Integer, Integer>> entry : monthCategoryCounts.entrySet()) {
			String month = entry.getKey();
			Map<Integer, Integer> categoryCounts = entry.getValue();

			List<Map<String, Object>> categories = new ArrayList<>();

			for (Categories categoriess : allCategories) {
				Integer categoryId = categoriess.getId();
				String categoryName = categoriess.getCategoryName();
				int totalItems = categoryCounts.getOrDefault(categoryId, 0);

				Map<String, Object> categoryItemCount = new HashMap<>();
				categoryItemCount.put("category", categoryName);
				categoryItemCount.put("totalItems", totalItems);

				categories.add(categoryItemCount);
			}

			Map<String, Object> monthData = new HashMap<>();
			monthData.put("month", month);
			monthData.put("categories", categories);

			response.add(monthData);
		}

		return response;
	}

	/**
	 * Retrieves the name of the month given its number.
	 * 
	 * This method converts the numeric month (1 for January, 2 for February, etc.)
	 * into its corresponding month name (e.g., "January", "February").
	 * 
	 * @param month The numeric representation of the month (1-12).
	 * @return The name of the month (e.g., "January").
	 */
	private String getMonthName(int month) {
		DateFormatSymbols dfs = new DateFormatSymbols();
		return dfs.getMonths()[month - 1];
	}

	/**
	 * Retrieves the count of items grouped by category for a specific month.
	 * 
	 * This method allows querying item counts by category for a specific month and
	 * year, provided in the format YYYY-MM. It validates the provided month format
	 * and ensures the month number is between 01 and 12. The method then queries
	 * the database for the item count in each category for the given month and
	 * year.
	 * 
	 * @param month The specific month in the format YYYY-MM (e.g., "2025-01").
	 * @return A list of maps where each map represents a category with its item
	 *         count for the specified month.
	 * @throws IllegalArgumentException If the provided month format is invalid or
	 *                                  the month is out of range.
	 */
	public List<Map<String, Object>> getItemCountByCategoryForSpecificMonth(String month) {
		LOGGER.info("fetch item count by category for the month: {}", month);
		if (month == null || !month.matches("\\d{4}-\\d{2}")) {
			LOGGER.error("Invalid month format provided: {}", month);

			throw new IllegalArgumentException("Invalid month format. Use YYYY-MM.");
		}

		int year = Integer.parseInt(month.split("-")[0]);
		int monthNumber = Integer.parseInt(month.split("-")[1]);

		if (monthNumber < 1 || monthNumber > 12) {
			LOGGER.error("Month number out of range: {}", monthNumber);

			throw new IllegalArgumentException("Month must be between 01 and 12.");
		}
		LOGGER.debug("Fetching item count for the year: {} and month: {}", year, monthNumber);

		List<Object[]> results = itemsRepository.findItemCountGroupedByCategoryIdAndMonth(year, monthNumber);

		List<Map<String, Object>> itemCountsByCategory = new ArrayList<>();

		for (Object[] result : results) {
			Map<String, Object> itemCountMap = new HashMap<>();

			int categoryId = ((Number) result[0]).intValue();

			long itemCount = ((Number) result[1]).longValue();

			Optional<Categories> categoryOptional = categoriesRepo.findById(categoryId);

			String categoryName = categoryOptional.map(Categories::getCategoryName).orElse("Unknown Category");

			itemCountMap.put("categoryName", categoryName);
			itemCountMap.put("itemCount", itemCount);

			itemCountsByCategory.add(itemCountMap);
		}
		LOGGER.info("Successfully fetched item count data for month: {}", month);

		return itemCountsByCategory;
	}

	/**
	 * This method fetches items for the current month and year, groups them by
	 * date, and sorts the items by uniqueId in descending order. It returns a map
	 * where the key is the date in the format "date:MM/dd/yyyy" and the value is a
	 * list of items that belong to that date.
	 * 
	 * @return A Map with keys as dates in the format "date:MM/dd/yyyy" and values
	 *         as lists of ItemSummaryDTO objects.
	 */
	public Map<String, List<ItemDTO>> getItemsGroupedByDate() {
		Map<String, List<ItemDTO>> groupedItems = new LinkedHashMap<>();

		try {
			LOGGER.info("Fetching items for the current month and year");

			List<ItemDTO> items = itemsRepository.findItemsByCurrentMonthAndYear();
			LOGGER.info("Retrieved {} items", items.size());

			items.sort(Comparator.comparing(ItemDTO::getUniqueId).reversed());
			LOGGER.info("Sorted items by uniqueId in descending order");

			groupedItems = items.stream().collect(Collectors.groupingBy(item -> {
				String uniqueId = item.getUniqueId();
				String datePart = uniqueId.split("-")[0];
				return "date:" + datePart;
			}, LinkedHashMap::new, Collectors.toList()));

			LOGGER.info("Grouped items into {} date categories", groupedItems.size());

		} catch (Exception e) {
			LOGGER.error("Error in getItemsGroupedByDate: ", e);
		}

		return groupedItems;
	}

	/**
	 * Searches for items based on specified criteria such as user email, received
	 * date, and status. 1. Accepts parameters for user email, received date, and
	 * item status to filter items. 2. Validates and converts the provided status
	 * string to an `ItemStatus` enum. 3. Retrieves the user ID associated with the
	 * provided email, if applicable. 4. Queries the repository to find items that
	 * match the provided criteria. 5. Returns a map containing the result of the
	 * search, including a success flag and either a list of items or an error
	 * message.
	 * 
	 * @param mail         The email address of the user (optional).
	 * @param receivedDate The date when the item was received (optional).
	 * @param status       The status of the item (optional). Should be one of the
	 *                     `ItemStatus` values.
	 * @return A map containing the search results, including the success status and
	 *         the data or error message.
	 */
	public Map<String, Object> searchItems(String mail, String receivedDate, String status) {
		Map<String, Object> response = new HashMap<>();
		try {
			LOGGER.info("Item search with email: {}, receivedDate: {}, status: {}", mail, receivedDate, status);

			ItemStatus itemStatus = null;
			if (status != null && !status.isEmpty()) {
				try {
					itemStatus = ItemStatus.valueOf(status.toUpperCase());
				} catch (IllegalArgumentException e) {
					LOGGER.error("Invalid status value provided: {}", status);
					throw new RuntimeException("Invalid status value provided: " + status, e);
				}
			}

			Integer userId = null;
			if (mail != null && !mail.isEmpty()) {
				LOGGER.info("Searching for user by email: {}", mail);
				userId = userRepository.findUserIdByEmail(mail);
				if (userId == null) {
					LOGGER.warn("No matching user found for email: {}", mail);
					response.put(Constants.MESSAGE, "No matching users");
					return response;
				}
			}

			Date date = null;
			if (receivedDate != null && !receivedDate.isEmpty()) {
				LOGGER.info("Received date input: {}", receivedDate);
				try {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					formatter.setLenient(false);
					date = formatter.parse(receivedDate);
					LOGGER.info("Parsed date successfully: {}", date);
				} catch (ParseException e) {
					LOGGER.error("Invalid date format provided: {}", receivedDate);
					throw new RuntimeException("Invalid date format. Expected format: yyyy-MM-dd", e);
				}
			}

			LOGGER.info("Searching items with userId: {}, itemStatus: {}, receivedDate: {}", userId, itemStatus, date);
			List<ItemDTO> items = itemsRepository.findAllByCriteria(userId, itemStatus, date);

			if (date != null) {
				LOGGER.info("Performing additional search for items with receivedDate: {}", date);
			}
			LOGGER.info("Searching items with userId: {}, itemStatus: {}, receivedDate: {}", userId, itemStatus, date);
			response.put(Constants.DATA, items);
			response.put(Constants.SUCCESS, true);
		} catch (RuntimeException e) {
			LOGGER.error("Error occurred during item search: {}", e.getMessage());
			response.put(Constants.MESSAGE, e.getMessage());
		}
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

			List<ItemsSearchDTO> results = (List<ItemsSearchDTO>) getAllItems();

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
			itemDetails.put("status", item.getStatus());
			itemDetails.put("receivedDate", item.getReceivedDate());
			itemDetails.put("title", item.getTitle());

			if (item.getUser() != null) {
				itemDetails.put("name", item.getUser().getUserName());
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
	 * Method to fetch the category name by category ID. It retrieves the category
	 * name associated with the given category ID from the repository.
	 *
	 * @param categoryId the ID of the category
	 * @return the name of the category, or 'Unknown Category' if not found
	 */
	private String getCategoryNameById(int categoryId) {
		LOGGER.info("Fetching category name for categoryId: {}", categoryId);

		Categories lookUp = categoriesRepo.findById(categoryId).orElse(null);
		if (lookUp != null) {
			LOGGER.info("Found category: {}", lookUp.getCategoryName());
			return lookUp.getCategoryName();
		} else {
			LOGGER.warn("Category not found for categoryId: {}", categoryId);
			return "Unknown Category";
		}
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
//	public Map<String, Object> getCarbonWeight() {
//		LOGGER.info("Starting category stats calculation.");
//
//		Map<String, Object> response = new HashMap<>();
//		Map<String, Double> categoryWeights = new HashMap<>();
//		Map<String, Integer> categoryItemCount = new HashMap<>();
//
//		List<Categories> allCategories = categoriesRepo.findCategoryIdAndCategoryName("C");
//		LOGGER.info("Found {} categories.", allCategories);
//
//		for (Categories lookUp : allCategories) {
//			System.out.println(lookUp);
//			String categoryName = lookUp.getCategoryName();
//			categoryWeights.put(categoryName, 0.0);
//			categoryItemCount.put(categoryName, 0);
//		}
//
//		List<Items> unclaimedItems = itemsRepository.findByStatus(ItemStatus.UNCLAIMED);
//		LOGGER.info("Found {} unclaimed items.", unclaimedItems.size());
//
//		for (Items item : unclaimedItems) {
//			int categoryId = item.getCategoryId();
////			LookUp lookUp = lookUpRepository.findAllById(categoryId).orElse(null);
//			Optional<Categories> lookUp = categoriesRepo.findById(categoryId);
//			System.out.println();
//
//			if (lookUp != null) {
//				Double weight = 0.0;
//				try {
//					weight = Double.parseDouble(item.getCarbonWeight().replace(" kg", "").trim());
//				} catch (NumberFormatException e) {
//					weight = 0.0;
//				}
//
//				String categoryName = lookUp.get().getCategoryName();
//				categoryWeights.put(categoryName, categoryWeights.getOrDefault(categoryName, 0.0) + weight);
//				categoryItemCount.put(categoryName, categoryItemCount.getOrDefault(categoryName, 0) + 1);
//			}
//		}
//
//		List<Map<String, String>> categories = new ArrayList<>();
//		for (String categoryName : categoryWeights.keySet()) {
//			Map<String, String> categoryStats = new HashMap<>();
//			categoryStats.put("category", categoryName);
//			categoryStats.put("totalWeight", String.format("%.2f kg", categoryWeights.get(categoryName)));
//			categoryStats.put("totalItems", String.valueOf(categoryItemCount.get(categoryName)));
//			categories.add(categoryStats);
//		}
//
//		response.put("categories", categories);
//		LOGGER.info("Category stats calculated: {}", categories);
//
//		return response;
//	}

	/**
	 * Method to fetch details of all items with the status 'ARCHIVED'. It retrieves
	 * all items with the specified status and returns the count and details in the
	 * response.
	 *
	 * @return a map containing the item count, item details, and a success message
	 */

//	public List<Items> getArchivedItems(String fromDateStr, String toDateStr) {
//		try {
//			if (fromDateStr != null && toDateStr != null && !fromDateStr.isEmpty() && !toDateStr.isEmpty()) {
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//				Date fromDate = sdf.parse(fromDateStr);
//				Date toDate = sdf.parse(toDateStr);
//
//				LOGGER.info("Fetching ARCHIVED items between {} and {}", fromDate, toDate);
//				return itemsRepository.findByStatusAndReceivedDateBetween(ItemStatus.ARCHIVED, fromDate, toDate);
//			} else {
//				LOGGER.info("Fetching all ARCHIVED items");
//				return itemsRepository.findByStatus(ItemStatus.ARCHIVED);
//			}
//		} catch (ParseException e) {
//			LOGGER.error("Error parsing dates", e);
//			return Collections.emptyList(); // Return empty list if date parsing fails
//		}
//	}
	public List<Items> getArchivedItems(String fromDateStr, String toDateStr) {
		try {
			if (fromDateStr != null && toDateStr != null && !fromDateStr.isEmpty() && !toDateStr.isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date fromDate = sdf.parse(fromDateStr);
				Date toDate = sdf.parse(toDateStr);

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(toDate);
				calendar.set(Calendar.HOUR_OF_DAY, 23);
				calendar.set(Calendar.MINUTE, 59);
				calendar.set(Calendar.SECOND, 59);
				calendar.set(Calendar.MILLISECOND, 999);
				toDate = calendar.getTime();

				LOGGER.info("Fetching ARCHIVED items between {} and {}", fromDate, toDate);
				return itemsRepository.findByStatusAndReceivedDateBetween(ItemStatus.ARCHIVED, fromDate, toDate);
			} else {
				LOGGER.info("Fetching all ARCHIVED items");
				return itemsRepository.findByStatus(ItemStatus.ARCHIVED);
			}
		} catch (ParseException e) {
			LOGGER.error("Error parsing dates", e);
			return Collections.emptyList(); // Return empty list if date parsing fails
		}
	}

	/**
	 * This method archives an expired item by updating its status to ARCHIVED. It
	 * only processes a single item at a time and requires an itemId to be provided.
	 * Checks if itemId is provided If itemId is null, it throws a RuntimeException
	 * with the message "Item ID must be provided." Finds the item in the database
	 * If the item exists, updates its status to ARCHIVED and saves it. If the item
	 * does not exist, it throws a RuntimeException with the message "Item not found
	 * with ID: {itemId}." Returns a response map with a success message if the
	 * operation is successful.
	 ***/
	public Map<String, Object> archiveExpiredItems(Integer itemId) {
		Map<String, Object> res = new HashMap<>();

		if (itemId != null) {
			Optional<Items> itemData = itemsRepository.findById(itemId);
			if (itemData.isPresent()) {
				Items item = itemData.get();

				item.setStatus(ItemStatus.ARCHIVED);
				itemsRepository.save(item);

				res.put(Constants.MESSAGE, "Item successfully archived.");
				res.put(Constants.SUCCESS, true);
			} else {
				throw new RuntimeException("Item not found with ID: " + itemId);
			}
		} else {
			throw new RuntimeException("Item ID must be provided.");
		}

		return res;
	}

}