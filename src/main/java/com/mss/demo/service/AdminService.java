
package com.mss.demo.service;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.DominantColorsAnnotation;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.mss.demo.constants.Constants;
import com.mss.demo.dto.ColorUtils;
import com.mss.demo.dto.ItemDTO;
import com.mss.demo.dto.ItemStatus;
import com.mss.demo.dto.ItemStatusCountDTO;
import com.mss.demo.dto.ItemSummaryDTO;
import com.mss.demo.dto.Login;
import com.mss.demo.entity.Categories;
import com.mss.demo.entity.ClaimHistory;
import com.mss.demo.entity.Items;
import com.mss.demo.entity.ItemsRequest;
import com.mss.demo.entity.Organisation;
import com.mss.demo.entity.Subcategories;
import com.mss.demo.entity.User;
import com.mss.demo.repo.CategoriesRepo;
import com.mss.demo.repo.ClaimHistoryRepository;
import com.mss.demo.repo.ItemRequestRepo;
import com.mss.demo.repo.ItemsRepo;
import com.mss.demo.repo.OrganisationRepository;
import com.mss.demo.repo.SubCategoriesRepo;
import com.mss.demo.repo.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class AdminService {

	@Autowired
	private ItemsRepo itemsRepository;

	@Autowired
	private ClaimHistoryRepository claimHistoryRepository;

	@Autowired
	private UserRepo userRepository;

	@Autowired
	private CategoriesRepo categoriesRepo;

	@Autowired
	private SubCategoriesRepo subcategoriesRepo;

	@Autowired
	private ItemRequestRepo itemRequestRepo;

	@Autowired
	private EmailService emailService;

	@Autowired
	private OrganisationRepository organisationRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);

	private static final String ADMIN_EMAIL = "admin@mail.com";
	private static final String ADMIN_PASSWORD = "admin123";
	private static final boolean IS_ADMIN = true;

	/**
	 * Authenticates a user based on the provided email, password, and admin status.
	 * 1. Checks if the email and password are not null. 2. Compares the provided
	 * credentials with the predefined admin credentials. 3. If the credentials
	 * match the admin credentials, a success response is generated for admin login.
	 * 4. If the credentials are incorrect or null, a failure response is generated
	 * with an appropriate message. 5. Returns a response entity containing a
	 * success flag, a message, and an admin status flag.
	 *
	 * @param login The login object containing email, password, and admin status.
	 * @return A ResponseEntity containing a map with success status, message, and
	 *         admin status flag.
	 */
	public ResponseEntity<Map<String, Object>> authenticate(Login login) {
		Map<String, Object> response = new HashMap<>();
		String email = login.getEmail();
		String password = login.getPassword();
		if (email != null && password != null) {
			LOGGER.info("Attempting authentication for email: {}", email);

			if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
				LOGGER.info("Authentication successful for admin with email: {}", email);
				response.put("success", true);
				response.put("message", "Authentication successful. Welcome, admin.");
				response.put("isAdmin", IS_ADMIN);
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				LOGGER.warn("Invalid credentials provided for email: {}", email);
				response.put("success", false);
				response.put("message", "Invalid email or password.");
				response.put("isAdmin", false);
			}
		} else {
			LOGGER.warn("Email or password is null during authentication attempt.");
		}
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}


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
	 * 
	 * 
	 */

	@Transactional
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
					.flatMap(label -> subcategoriesRepo.findByNameLikeIgnoreCase(label.toLowerCase()).stream())
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
					.flatMap(label -> subcategoriesRepo.findByNameLikeIgnoreCase(label.toLowerCase()).stream())
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
	 * Saves the claim history for an item based on the provided claim history data.
	 * 
	 * This method: 1. Retrieves the request associated with the item ID. 2. Creates
	 * and saves a claim history record with the provided status and details. 3. If
	 * the claim status is "CLAIMED", updates the request and item status to
	 * "CLAIMED". 4. Handles errors by logging issues and providing a relevant
	 * message in the response. // confirm by user or admin
	 * 
	 * @param claimHistoryDTO The data transfer object containing claim details.
	 * @return A map containing the success status and a message indicating the
	 *         result of the operation.
	 */

	public Map<String, Object> saveRequestHistory(ClaimHistory claimHistoryDTO) {
		LOGGER.info("Entering saveRequestHistory method with claimHistoryDTO: {}", claimHistoryDTO);
		Map<String, Object> res = new HashMap<>();

		try {
			int itemId = claimHistoryDTO.getItemId();
			LOGGER.debug("Item ID: {}", itemId);
			Optional<ItemsRequest> requestOpt = itemRequestRepo.findByItemId(itemId);
			if (requestOpt.isEmpty()) {
				LOGGER.error("Request with item ID {} not found.", itemId);
				throw new RuntimeException("Request with item ID " + itemId + " not found.");
			}

			ItemsRequest itemsRequest = requestOpt.get();

			ClaimHistory claimHistory = new ClaimHistory();
			claimHistory.setClaimStatus(claimHistoryDTO.getClaimStatus());
			claimHistory.setClaimId(claimHistoryDTO.getClaimId());
			claimHistory.setClaimDate(
					claimHistoryDTO.getClaimDate() != null ? claimHistoryDTO.getClaimDate() : LocalDateTime.now());
			claimHistory.setUser(itemsRequest.getUser());
			claimHistory.setUserEmail(itemsRequest.getUser().getEmail()); // Assuming User has an email field
			claimHistory.setRequest(itemsRequest);
			claimHistory.setItemId(itemsRequest.getItem().getItemId());

			claimHistoryRepository.save(claimHistory);
			LOGGER.info("Claim history saved successfully for item ID {}", itemId);

			if (claimHistoryDTO.getClaimStatus() == ItemStatus.CLAIMED) {
				itemsRequest.setStatus(ItemStatus.CLAIMED);
				itemRequestRepo.save(itemsRequest);

				Optional<Items> itemsOpt = itemsRepository.findById(itemsRequest.getItem().getItemId());
				if (itemsOpt.isEmpty()) {
					LOGGER.error("Item with ID {} not found.", itemsRequest.getItem().getItemId());
					throw new RuntimeException("Item with ID " + itemsRequest.getItem().getItemId() + " not found.");
				}

				Items item = itemsOpt.get();
				item.setStatus(ItemStatus.CLAIMED);
				itemsRepository.save(item);

				LOGGER.info("Item and request status updated to CLAIMED for item ID {}", itemId);

				res.put("message", "Approved successfully.");
				res.put("success", true);
			} else {
				LOGGER.warn("Unsupported claim status: {}", claimHistoryDTO.getClaimStatus());
				res.put("message", "Unsupported status. Only CLAIMED status is handled.");
				res.put("success", false);
			}
		} catch (Exception e) {
			res.put(Constants.MESSAGE, "Unexpected error occurred while searching items.");
			LOGGER.error("Unexpected error in saveRequestHistory: {}", e.getMessage(), e);

		}
		LOGGER.info("Exiting saveRequestHistory method with response: {}", res);
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

	public Map<String, List<ItemSummaryDTO>> getAllGroupedItemsSortedByDateDesc() {
		LOGGER.info("Entering getAllGroupedItemsSortedByDateDesc method.");
		Map<String, List<ItemSummaryDTO>> groupedItems = new LinkedHashMap<>();
		try {
			List<ItemSummaryDTO> items = itemsRepository.findItemsSummaryByDate();

			Map<String, List<ItemSummaryDTO>> allGroupedItems = items.stream()
					.collect(Collectors.groupingBy(item -> "date:" + item.getUniqueId().split("-")[0]));

			groupedItems = allGroupedItems.entrySet().stream().sorted((e1, e2) -> {
				String date1 = e1.getKey().substring(5); 
				String date2 = e2.getKey().substring(5);
				LocalDate parsedDate1 = LocalDate.parse(date1, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
				LocalDate parsedDate2 = LocalDate.parse(date2, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
				return parsedDate2.compareTo(parsedDate1); 
			}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue,
					LinkedHashMap::new));
		} catch (Exception e) {
			LOGGER.error("Error in getAllGroupedItemsSortedByDateDesc: ", e);
		}
		return groupedItems;
	}

	public Map<String, Object> getAllItems() {
		LOGGER.info("Entering getAllItems method.");
		Map<String, Object> response = new HashMap<>();
		try {
			List<ItemSummaryDTO> items = itemsRepository.findItemsSummary();
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
	 * Retrieves a list of categories with specific fields.
	 * 
	 * This method: 1. Retrieves categories from the categories repository,
	 * filtering specific fields. 2. Returns the list of categories.
	 * 
	 * @return A list of categories with selected fields.
	 */
	public List<Map<String, Object>> getCategories() {
		LOGGER.info("Entering getCategories");

		List<Object[]> results = categoriesRepo.findCategoryIdAndName();

		List<Map<String, Object>> resList = results.stream().map(result -> {
			Map<String, Object> categoryMap = new HashMap<>();

			LOGGER.info("Category ID: {}, Category Name: {}", result[0], result[1]);

			categoryMap.put("id", result[0]);
			categoryMap.put("name", result[1]);

			return categoryMap;
		}).collect(Collectors.toList());

		return resList;
	}

	/**
	 * Claims an item for a user by creating a claim request.
	 * 
	 * This method: 1. Accepts a claim request DTO containing the user's email, item
	 * ID, and user name. 2. Verifies the inputs, ensures that the item and user
	 * exist, and processes the claim request. 3. If the item does not have an
	 * existing claim, creates a new claim request and updates the item status to
	 * "Pending Approval." 4. Sends a confirmation email to the user and a
	 * notification to the admin.
	 * 
	 * @param claimRequest The claim request containing user details and item ID.
	 * @return A response map indicating success or failure, including the relevant
	 *         message.
	 */
	public Map<String, Object> claimItem(Map<String, Object> requestData) {
		LOGGER.info("Entering claimItem with requestData: {}", requestData);
		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		String name = (String) requestData.get("name");
		String email = (String) requestData.get("email");
		Integer itemId = (Integer) requestData.get("itemId");

		if (email == null || email.isEmpty() || itemId == null || itemId <= 0) {
			LOGGER.info("Invalid user input: Name, email, or itemId is null/empty.");
			return res;
		}

		User user = new User();
		user.setName(name);
		user.setEmail(email);

		return processClaim(user, itemId);
	}

	private Map<String, Object> processClaim(User user, int itemId) {
		LOGGER.info("Processing claim for user: {} and itemId: {}", user, itemId);
		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		User userData = userRepository.findByEmail(user.getEmail()).orElse(null);
		if (userData == null) {
			LOGGER.info("User not found, creating a new user.");
			userData = new User();
			userData.setName(user.getName());
			userData.setEmail(user.getEmail());
			userData = userRepository.save(userData);
		}

		Items item = itemsRepository.findById(itemId).orElse(null);
		if (item == null) {
			LOGGER.info("Item not found with ID: {}", itemId);
			res.put(Constants.MESSAGE, "Item not found.");
			return res;
		}

		if (item.getUser() != null) {
			LOGGER.info("Item with ID {} is already claimed.", itemId);
			res.put(Constants.MESSAGE, "Item is already claimed by another user.");
			return res;
		}

		item.setUser(userData);
		item.setStatus(ItemStatus.PENDING_APPROVAL);
		item.setReceivedDate(new Date());
		itemsRepository.save(item);

		ItemsRequest newRequest = new ItemsRequest();
		newRequest.setItem(item);
		newRequest.setUser(userData);
		newRequest.setStatus(ItemStatus.PENDING_APPROVAL);
		itemRequestRepo.save(newRequest);

		String message = "The item " + item.getTitle() + " has been claimed by " +
                item.getUser().getName();

		userData.setMessage(message);
		userRepository.save(userData); 

		emailService.sendClaimConfirmationEmail(user.getEmail(), item);
		emailService.sendClaimNotificationToAdmin(item);

		res.put(Constants.MESSAGE, "Successfully claimed the item.");
		res.put(Constants.SUCCESS, true);
		return res;
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
//	public Map<String, Object> archiveExpiredItems(Integer itemId) {
//		LOGGER.info("Entering archiveExpiredItems with itemId: {}", itemId);
//		Map<String, Object> res = new HashMap<>();
//		res.put(Constants.SUCCESS, false);
//		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);
//
//		Date currentDate = new Date();
//
//		if (itemId == null) {
//			List<Items> expiredItems = itemsRepository.findAll();
//
//			for (Items item : expiredItems) {
//				Date receivedDate = item.getReceivedDate();
//				if (receivedDate == null) {
//					continue;
//				}
//
//				Calendar calendar = Calendar.getInstance();
//				calendar.setTime(receivedDate);
//				calendar.add(Calendar.DAY_OF_MONTH, 90);
//				Date expirationDate = calendar.getTime();
//
//				long timeUntilExpiration = expirationDate.getTime() - currentDate.getTime();
//				long daysUntilExpiration = TimeUnit.MILLISECONDS.toDays(timeUntilExpiration);
//
//				if (daysUntilExpiration == 30) {
//					if (item.getUser() != null) {
//						emailService.sendExpirationReminder(item.getUser().getEmail(), item, 30);
//					} else {
//						LOGGER.info("User is null for item with ID: " + item.getItemId());
//
//					}
//				} else if (daysUntilExpiration == 3) {
//					if (item.getUser() != null) {
//						emailService.sendExpirationReminder(item.getUser().getEmail(), item, 3);
//					} else {
//						LOGGER.info("User is null for item with ID: " + item.getItemId());
//					}
//				}
//
//				if (currentDate.after(expirationDate)) {
//					item.setStatus(ItemStatus.ARCHIVED);
//					itemsRepository.save(item);
//
//					emailService.sendArchivedNotificationToAdmin(item);
//					if (item.getUser() != null) {
//						emailService.sendArchivedNotificationToUser(item);
//					} else {
//						LOGGER.info("User is null for item with ID: " + item.getItemId());
//					}
//				}
//			}
//		} else {
//			Optional<Items> itemData = itemsRepository.findById(itemId);
//			if (itemData.isPresent()) {
//				Items item = itemData.get();
//
//				Date receivedDate = item.getReceivedDate();
//				if (receivedDate == null) {
//					throw new RuntimeException("Found date is missing for the item.");
//				}
//
//				Calendar calendar = Calendar.getInstance();
//				calendar.setTime(receivedDate);
//				calendar.add(Calendar.DAY_OF_MONTH, 90);
//				Date expirationDate = calendar.getTime();
//
//				long timeUntilExpiration = expirationDate.getTime() - currentDate.getTime();
//				long daysUntilExpiration = TimeUnit.MILLISECONDS.toDays(timeUntilExpiration);
//
//				if (daysUntilExpiration == 30) {
//					if (item.getUser() != null) {
//						emailService.sendExpirationReminder(item.getUser().getEmail(), item, 30);
//					} else {
//						LOGGER.info("User is null for item with ID: " + item.getItemId());
//					}
//				} else if (daysUntilExpiration == 3) {
//					if (item.getUser() != null) {
//						emailService.sendExpirationReminder(item.getUser().getEmail(), item, 3);
//					} else {
//						LOGGER.info("User is null for item with ID: " + item.getItemId());
//					}
//				}
//
//				if (currentDate.after(expirationDate)) {
//					item.setStatus(ItemStatus.ARCHIVED);
//					itemsRepository.save(item);
//
//					emailService.sendArchivedNotificationToAdmin(item);
//					if (item.getUser() != null) {
//						emailService.sendArchivedNotificationToUser(item);
//					} else {
//						LOGGER.info("User is null for item with ID: " + item.getItemId());
//					}
//				}
//			} else {
//				throw new RuntimeException("Item not found with ID: " + itemId);
//			}
//		}
//
//		res.put(Constants.MESSAGE, Constants.SUCESSFULLY_DELETED);
//		res.put(Constants.SUCCESS, true);
//		return res;
//	}
	@Transactional
	public Map<String, Object> archiveExpiredItems(Integer itemId) {
		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		if (itemId == null) {
			res.put(Constants.MESSAGE, "Item ID cannot be null.");
			return res;
		}

		int updatedRows = itemsRepository.updateStatusById(ItemStatus.ARCHIVED, itemId);
		if (updatedRows > 0) {
			res.put(Constants.SUCCESS, true);
			res.put(Constants.MESSAGE, "Item archived successfully.");
		} else {
			res.put(Constants.MESSAGE, "Item not found or status not updated.");
		}

		return res;
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

			// Validate and parse status
			ItemStatus itemStatus = null;
			if (status != null && !status.isEmpty()) {
				try {
					itemStatus = ItemStatus.valueOf(status.toUpperCase());
				} catch (IllegalArgumentException e) {
					LOGGER.error("Invalid status value provided: {}", status);
					throw new RuntimeException("Invalid status value provided: " + status, e);
				}
			}

			// Validate and find user by email
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

			// Validate and parse receivedDate
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
//	            List<ItemDTO> itemsDate = itemsRepository.findByReceivedDate(date);
//	            items.addAll(itemsDate);
			}
			LOGGER.info("Searching items with userId: {}, itemStatus: {}, receivedDate: {}", userId, itemStatus, date);
//			List<ItemDTO> items = itemsRepository.findAllByCriteria(userId, itemStatus, date);
			response.put(Constants.DATA, items);
			response.put(Constants.SUCCESS, true);
		} catch (RuntimeException e) {
			LOGGER.error("Error occurred during item search: {}", e.getMessage());
			response.put(Constants.MESSAGE, e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Unexpected error occurred while searching items: {}", e.getMessage());
			response.put(Constants.MESSAGE, "Unexpected error occurred while searching items.");
		}
		return response;
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
			response.put(Constants.MESSAGE, "Status cannot be null.");
			response.put("status", HttpStatus.BAD_REQUEST);
			return response;
		}

		Optional<ItemsRequest> requestOpt = itemRequestRepo.findFirstByItem_ItemIdAndStatus(itemId,
				ItemStatus.PENDING_APPROVAL);
		Optional<Items> items = itemsRepository.findById(itemId);
		items.get().setStatus(status);

		if (requestOpt.isEmpty()) {
			response.put(Constants.MESSAGE, "Already status was updated for Item ID: " + itemId);
			response.put("status", status);
			return response;
		}

		ItemsRequest itemsRequest = requestOpt.get();

		if (status == ItemStatus.REJECTED) {
			if (rejectedReason == null || rejectedReason.trim().isEmpty()) {
				response.put(Constants.MESSAGE, "Rejection reason is required for status REJECTED.");
				response.put("status", HttpStatus.BAD_REQUEST);
				return response;
			}
			itemsRequest.setRejectedReason(rejectedReason);
		}

		itemsRequest.setClaimedDate(LocalDateTime.now());
		itemsRequest.setStatus(status);
		itemRequestRepo.save(itemsRequest);

		items.get().setStatus(status);
		itemsRepository.save(items.get());

		response.put(Constants.MESSAGE, Constants.SUCESSFULLY_UPDATED);
		response.put("status", HttpStatus.OK);
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
		List<Categories> allCategories = categoriesRepo.findAll();
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

			for (Categories category : allCategories) {
				Integer categoryId = category.getId();
				String categoryName = category.getCategoryName();
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
		if (month == null || !month.matches("\\d{4}-\\d{2}")) {
			throw new IllegalArgumentException("Invalid month format. Use YYYY-MM.");
		}

		int year = Integer.parseInt(month.split("-")[0]);
		int monthNumber = Integer.parseInt(month.split("-")[1]);

		if (monthNumber < 1 || monthNumber > 12) {
			throw new IllegalArgumentException("Month must be between 01 and 12.");
		}

		List<Object[]> results = itemsRepository.findItemCountGroupedByCategoryIdAndMonth(year, monthNumber);

		List<Map<String, Object>> itemCountsByCategory = new ArrayList<>();

		for (Object[] result : results) {
			Map<String, Object> itemCountMap = new HashMap<>();

			int categoryId = ((Number) result[0]).intValue();

			long itemCount = ((Number) result[1]).longValue();

			Optional<Categories> categoryOptional = categoriesRepo.findById(categoryId);

			String categoryName;
			if (categoryOptional.isPresent()) {
				categoryName = categoryOptional.get().getCategoryName();
			} else {
				LOGGER.warn("Category ID {} not found in Categories table.", categoryId);
				continue; // Skip this category if not found
			}

			itemCountMap.put("categoryName", categoryName);
			itemCountMap.put("itemCount", itemCount);

			itemCountsByCategory.add(itemCountMap);
		}

		return itemCountsByCategory;
	}

	/**
	 * Retrieves all claim requests from the repository.
	 * 
	 * This method queries the database for all claim requests and returns them as a
	 * list. The response contains a status indicating whether the operation was
	 * successful or if an error occurred. In case of success, the data is returned
	 * in the response; otherwise, an error message is included.
	 * 
	 * @return A map containing the status and data. If successful, the claims are
	 *         included in the 'data' field.
	 */
	public Map<String, Object> getAllClaims() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<User> users = userRepository.findAll();

			List<Map<String, Object>> simplifiedClaims = new ArrayList<>();

			for (User user : users) {
				for (Items item : user.getItems()) {
					
					Map<String, Object> claimData = new HashMap<>();
					claimData.put("name", user.getName()); 
					claimData.put("message", user.getMessage()); 
					claimData.put("read", user.isRead()); 

					simplifiedClaims.add(claimData); 
				}
			}

			response.put("data", simplifiedClaims);

		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "An error occurred: " + e.getMessage());
		}

		return response;
	}

	/**
	 * Updates an existing claim request.
	 * 
	 * This method updates the details of an existing claim request based on the
	 * provided claim request object. It checks if the provided claim request exists
	 * in the repository, then updates its name, message, and email fields if they
	 * are provided. Additionally, it sets the 'read' status of the claim request to
	 * true and saves the updated claim request back to the repository.
	 * 
	 * @param claimRequest The claim request object containing updated information.
	 * @return A map containing the success status and a message indicating whether
	 *         the update was successful or if an error occurred.
	 */
	public Map<String, Object> updateClaimRequest(User user) {
		Map<String, Object> response = new HashMap<>();
		response.put(Constants.SUCCESS, false);
		response.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		boolean isRead = true;

		Optional<User> requestOpt = userRepository.findById(user.getUserId());

		if (requestOpt.isEmpty()) {
			response.put(Constants.MESSAGE, "Claim request not found.");
			return response;
		}

		User existingRequest = requestOpt.get();

		if (user.getName() != null) {
			existingRequest.setName(user.getName());
		}
		if (user.getMessage() != null) {
			existingRequest.setMessage(user.getMessage());
		}
		if (user.getEmail() != null) {
			existingRequest.setEmail(user.getEmail());
		}

		existingRequest.setRead(isRead);

		User updatedRequest = userRepository.save(existingRequest);

		if (updatedRequest != null) {
			response.put(Constants.SUCCESS, true);
			response.put(Constants.MESSAGE, Constants.SUCESSFULLY_UPDATED);
		} else {
			response.put(Constants.MESSAGE, Constants.SOMETHING_WENT_WRONG);
		}

		return response;
	}

}