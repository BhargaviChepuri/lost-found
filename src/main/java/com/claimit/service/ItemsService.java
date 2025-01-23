package com.claimit.service;

import java.awt.Color;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

import com.claimit.constants.Constants;
import com.claimit.dto.CategoriesDTO;
import com.claimit.dto.ClaimRequestDTO;
import com.claimit.dto.ColorUtils;
import com.claimit.dto.ItemDTO;
import com.claimit.dto.ItemStatus;
import com.claimit.dto.ItemStatusCountDTO;
import com.claimit.dto.ItemSummaryDTO;
import com.claimit.dto.ItemsSearchDTO;
import com.claimit.entity.ClaimHistory;
import com.claimit.entity.ClaimRequest;
import com.claimit.entity.Items;
import com.claimit.entity.ItemsRequest;
import com.claimit.entity.Login;
import com.claimit.entity.LookUp;
import com.claimit.entity.Organisation;
import com.claimit.entity.User;
import com.claimit.exception.ItemNotFoundException;
import com.claimit.repo.ClaimHistoryRepository;
import com.claimit.repo.ClaimRequestRepository;
import com.claimit.repo.ItemRequestRepo;
import com.claimit.repo.ItemsRepo;
import com.claimit.repo.LookUpRepository;
import com.claimit.repo.OrganisationRepository;
import com.claimit.repo.UserRepo;
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
	private ClaimHistoryRepository claimHistoryRepository;

	@Autowired
	private UserRepo userRepository;

	@Autowired
	private ItemRequestRepo itemRequestRepo;

	@Autowired
	private ClaimRequestRepository claimRequestRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private OrganisationRepository organisationRepository;

	@Autowired
	private LookUpRepository lookUpRepository;

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
	 * 
	 * 
	 */

	public Map<String, Object> detectLabels(MultipartFile file, String orgId) throws IOException {

		LOGGER.info(" label detection for organization ID: {}", orgId);

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
			LOGGER.info("Image analysis Successfully for organization ID: {}", orgId);

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

			List<String> detectedLabels = response.getLabelAnnotationsList().stream()
					.map(EntityAnnotation::getDescription).distinct().collect(Collectors.toList());

			String title = !detectedLabels.isEmpty() ? detectedLabels.get(0) : "Unknown Item";
			String description = "Detected Labels: " + String.join(", ", detectedLabels);
//
			List<LookUp> matchedSubcategories = detectedLabels.stream()
					.flatMap(label -> lookUpRepository.findByNameLikeIgnoreCase(label.toLowerCase()).stream())
					.distinct().collect(Collectors.toList());
			List<LookUp> matchedCategories = detectedLabels.stream()
					.flatMap(label -> lookUpRepository.findByNameLikeCase(label.toLowerCase()).stream()).distinct()
					.collect(Collectors.toList());

			if (!matchedSubcategories.isEmpty()) {
				LookUp matchedSubcategory = matchedSubcategories.get(0);
				LookUp matchedCategory = matchedCategories.get(0);

				item.setSubcatgeoryId(matchedSubcategory.getId());
				LOGGER.info("Matched subcategory ID: {} for detected labels", matchedSubcategory.getId());

				item.setCategoryId(matchedCategory.getId());
				LOGGER.info("Matched category ID: {} for detected labels", matchedCategory.getId());

			} else {
//				item.setSubcatgeoryId(30);
//				item.setCategoryId(7);
				LOGGER.info("No matching subcategories found, setting default category and subcategory.");

			}
			String itemName = detectedLabels.isEmpty() ? "Unknown Item"
					: String.join(", ", detectedLabels.subList(0, Math.min(3, detectedLabels.size())));
			item.setItemName(itemName);

			if (response.hasImagePropertiesAnnotation()) {
				DominantColorsAnnotation colors = response.getImagePropertiesAnnotation().getDominantColors();
				String mostDominantColor = colors.getColorsList().stream()
						.max((colorInfo1, colorInfo2) -> Float.compare(colorInfo1.getScore(), colorInfo2.getScore()))
						.map(colorInfo -> ColorUtils.getClosestColorName((int) colorInfo.getColor().getRed(),
								(int) colorInfo.getColor().getGreen(), (int) colorInfo.getColor().getBlue()))
						.orElse("Unknown");
				item.setDominantColor(mostDominantColor);
				description += ", Dominant Color: " + mostDominantColor;
				res.put("colour", mostDominantColor);
				LOGGER.info("Detected dominant color: {}", mostDominantColor);

			}

			List<String> texts = response.getTextAnnotationsList().stream().map(EntityAnnotation::getDescription)
					.collect(Collectors.toList());
			String detectedText = texts.isEmpty() ? "None" : String.join(", ", texts);
			item.setDetectedText(detectedText);
			description += ", Text Detected: " + detectedText;

			LocalDate currentDate = LocalDate.now();
			String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

			int latestNumber = itemsRepository.findLatestNumberByDate(formattedDate).orElse(0);

			// Increment the number or start with 1 for a new day
			int newNumber = latestNumber + 1;

			// Create the uniqueId
			String uniqueId = formattedDate + "-" + newNumber;

			String carbonWeight = calculateCarbonWeight(detectedLabels, item.getCategoryId());
			item.setCarbonWeight(carbonWeight);

			item.setTitle(title);
			item.setDescription(description);
			item.setOrgId(orgId);
			item.setStatus(ItemStatus.UNCLAIMED);
			item.setUniqueId(uniqueId);
			itemsRepository.save(item);
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
	 * Updates the image of an existing item in the database.
	 * 
	 * This method: 1. Retrieves the item by its ID from the repository. 2. If the
	 * item is found, updates the item's image with the provided byte array. 3.
	 * Saves the updated item back to the repository. 4. Returns a success message
	 * indicating the image was successfully updated.
	 * 
	 * @param image  The new image data as a byte array.
	 * @param itemId The ID of the item whose image needs to be updated.
	 * @return A success message indicating the result of the image update
	 *         operation.
	 * @throws RuntimeException if the item with the given ID is not found.
	 */
	public String updateItemsImage(byte[] image, int itemId) {
		LOGGER.info("Entering updateItemsImage method with itemId: {}", itemId);
		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		Optional<Items> itemOptional = itemsRepository.findById(itemId);
		if (!itemOptional.isPresent()) {
			throw new RuntimeException("Item with ID " + itemId + " not found.");
		}

		Items item = itemOptional.get();
		item.setImage(image);

		itemsRepository.save(item);

		return "Image updated successfully for item ID: " + itemId;
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
	public Map<String, Object> claimItem(ClaimRequestDTO claimRequest) {
		LOGGER.info("Entering claimItem with claimRequest: {}", claimRequest);
		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		String userEmail = claimRequest.getUserEmail();
		int itemId = claimRequest.getItemId();
		String name = claimRequest.getUserName();

		if (itemId > 0 && userEmail != null && !userEmail.isEmpty()) {
			LOGGER.info("Valid inputs received.");

			Items item = itemsRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

			User user = userRepository.findByEmail(userEmail).orElse(null);
			if (user == null) {
				LOGGER.info("User not found, creating new user.");
				user = new User();
				user.setUserName(name);
				user.setEmail(userEmail);

				user = userRepository.save(user);
			}

			if (item.getUser() == null) {
				item.setUser(user);
				itemsRepository.save(item);
				itemsRepository.flush();
			}

			item = itemsRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

			ItemsRequest existingRequest = itemRequestRepo
					.findFirstByItem_ItemIdAndStatus(itemId, ItemStatus.PENDING_APPROVAL).orElse(null);

			if (existingRequest == null) {
				ItemsRequest newRequest = new ItemsRequest();
				newRequest.setItem(item);
				newRequest.setUser(user);
				newRequest.setStatus(ItemStatus.PENDING_APPROVAL);
				itemRequestRepo.save(newRequest);

				item.setStatus(ItemStatus.PENDING_APPROVAL);
				item.setReceivedDate(new Date());
				itemsRepository.save(item);

				emailService.sendClaimConfirmationEmail(userEmail, item);
				emailService.sendClaimNotificationToAdmin(item);

				res.put(Constants.MESSAGE, Constants.SUCESSFULLY_ADDED);
				res.put(Constants.SUCCESS, true);
			} else {
				LOGGER.info("A claim request is already in progress for this item.");
				res.put(Constants.MESSAGE, "A claim request is already in progress for this item.");
			}

		} else {
			LOGGER.info("Invalid inputs: itemId or userEmail is invalid.");

		}

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
	public Map<String, Object> archiveExpiredItems(Integer itemId) {
		LOGGER.info("Entering archiveExpiredItems with itemId: {}", itemId);
		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		Date currentDate = new Date();

		if (itemId == null) {
			List<Items> expiredItems = itemsRepository.findAll();

			for (Items item : expiredItems) {
				Date receivedDate = item.getReceivedDate();
				if (receivedDate == null) {
					continue;
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(receivedDate);
				calendar.add(Calendar.DAY_OF_MONTH, 90);
				Date expirationDate = calendar.getTime();

				long timeUntilExpiration = expirationDate.getTime() - currentDate.getTime();
				long daysUntilExpiration = TimeUnit.MILLISECONDS.toDays(timeUntilExpiration);

				if (daysUntilExpiration == 30) {
					if (item.getUser() != null) {
						emailService.sendExpirationReminder(item.getUser().getEmail(), item, 30);
					} else {
						LOGGER.info("User is null for item with ID: " + item.getItemId());

					}
				} else if (daysUntilExpiration == 3) {
					if (item.getUser() != null) {
						emailService.sendExpirationReminder(item.getUser().getEmail(), item, 3);
					} else {
						LOGGER.info("User is null for item with ID: " + item.getItemId());
					}
				}

				if (currentDate.after(expirationDate)) {
					item.setStatus(ItemStatus.ARCHIVED);
					itemsRepository.save(item);

					emailService.sendArchivedNotificationToAdmin(item);
					if (item.getUser() != null) {
						emailService.sendArchivedNotificationToUser(item);
					} else {
						LOGGER.info("User is null for item with ID: " + item.getItemId());
					}
				}
			}
		} else {
			Optional<Items> itemData = itemsRepository.findById(itemId);
			if (itemData.isPresent()) {
				Items item = itemData.get();

				Date receivedDate = item.getReceivedDate();
				if (receivedDate == null) {
					throw new RuntimeException("Found date is missing for the item.");
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(receivedDate);
				calendar.add(Calendar.DAY_OF_MONTH, 90);
				Date expirationDate = calendar.getTime();

				long timeUntilExpiration = expirationDate.getTime() - currentDate.getTime();
				long daysUntilExpiration = TimeUnit.MILLISECONDS.toDays(timeUntilExpiration);

				if (daysUntilExpiration == 30) {
					if (item.getUser() != null) {
						emailService.sendExpirationReminder(item.getUser().getEmail(), item, 30);
					} else {
						LOGGER.info("User is null for item with ID: " + item.getItemId());
					}
				} else if (daysUntilExpiration == 3) {
					if (item.getUser() != null) {
						emailService.sendExpirationReminder(item.getUser().getEmail(), item, 3);
					} else {
						LOGGER.info("User is null for item with ID: " + item.getItemId());
					}
				}

				if (currentDate.after(expirationDate)) {
					item.setStatus(ItemStatus.ARCHIVED);
					itemsRepository.save(item);

					emailService.sendArchivedNotificationToAdmin(item);
					if (item.getUser() != null) {
						emailService.sendArchivedNotificationToUser(item);
					} else {
						LOGGER.info("User is null for item with ID: " + item.getItemId());
					}
				}
			} else {
				throw new RuntimeException("Item not found with ID: " + itemId);
			}
		}

		res.put(Constants.MESSAGE, Constants.SUCESSFULLY_DELETED);
		res.put(Constants.SUCCESS, true);
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
	public Map<String, Object> searchItems(String mail, Date receivedDate, String status) {
		Map<String, Object> response = new HashMap<>();
		response.put(Constants.SUCCESS, false);
		response.put(Constants.MESSAGE, Constants.INVALID_INPUTS);
		try {
			LOGGER.info(" item search with email: {}, receivedDate: {}, status: {}", mail, receivedDate, status);

			ItemStatus itemStatus = null;
			if (status != null && !status.isEmpty()) {
				try {
					LOGGER.info(" item search with email: {}, receivedDate: {}, status: {}", mail, receivedDate,
							status);
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
			LOGGER.info("Searching items with userId: {}, itemStatus: {}, receivedDate: {}", userId, itemStatus,
					receivedDate);

			List<ItemDTO> items = itemsRepository.findAllByCriteria(userId, itemStatus, receivedDate);

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
			LOGGER.warn("Received null status for itemId: {}", itemId);

			response.put(Constants.MESSAGE, "Status cannot be null.");
			response.put("status", HttpStatus.BAD_REQUEST);
			return response;
		}
		LOGGER.info(" approval/rejection process for Item ID: {}", itemId);

		Optional<ItemsRequest> requestOpt = itemRequestRepo.findFirstByItem_ItemIdAndStatus(itemId,
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
		List<LookUp> allCategories = lookUpRepository.findAll();
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

			for (LookUp lookUp : allCategories) {
				Integer categoryId = lookUp.getId();
				String categoryName = lookUp.getName();
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

			Optional<LookUp> categoryOptional = lookUpRepository.findById(categoryId);

			String categoryName = categoryOptional.map(LookUp::getName).orElse("Unknown Category");

			itemCountMap.put("categoryName", categoryName);
			itemCountMap.put("itemCount", itemCount);

			itemCountsByCategory.add(itemCountMap);
		}
		LOGGER.info("Successfully fetched item count data for month: {}", month);

		return itemCountsByCategory;
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
	public Map<String, Object> updateClaimRequest(ClaimRequest claimRequest) {
		Map<String, Object> response = new HashMap<>();
		response.put(Constants.SUCCESS, false);
		response.put(Constants.MESSAGE, Constants.INVALID_INPUTS);

		LOGGER.info(" update for ClaimRequest with ID: {}", claimRequest.getId());

		boolean isRead = true;

		Optional<ClaimRequest> requestOpt = claimRequestRepository.findById(claimRequest.getId());

		if (requestOpt.isEmpty()) {
			LOGGER.warn("Claim request with ID: {} not found.", claimRequest.getId());

			response.put(Constants.MESSAGE, "Claim request not found.");
			return response;
		}

		ClaimRequest existingRequest = requestOpt.get();

		if (claimRequest.getName() != null) {
			LOGGER.info("Updating name for ClaimRequest ID: {}", claimRequest.getId());

			existingRequest.setName(claimRequest.getName());
		}
		if (claimRequest.getMessage() != null) {
			LOGGER.info("Updating message for ClaimRequest ID: {}", claimRequest.getId());

			existingRequest.setMessage(claimRequest.getMessage());
		}
		if (claimRequest.getEmail() != null) {
			LOGGER.info("Updating email for ClaimRequest ID: {}", claimRequest.getId());

			existingRequest.setEmail(claimRequest.getEmail());
		}

		existingRequest.setRead(isRead);
		LOGGER.info("Setting 'read' flag to true for ClaimRequest ID: {}", claimRequest.getId());

		ClaimRequest updatedRequest = claimRequestRepository.save(existingRequest);

		if (updatedRequest != null) {
			LOGGER.info("Successfully updated ClaimRequest with ID: {}", claimRequest.getId());

			response.put(Constants.SUCCESS, true);
			response.put(Constants.MESSAGE, Constants.SUCESSFULLY_UPDATED);
		} else {
			LOGGER.error("Failed to update ClaimRequest with ID: {}", claimRequest.getId());

			response.put(Constants.MESSAGE, Constants.SOMETHING_WENT_WRONG);
		}

		return response;
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
	public Map<String, List<ItemSummaryDTO>> getItemsGroupedByDate() {
		Map<String, List<ItemSummaryDTO>> groupedItems = new LinkedHashMap<>();

		try {
			LOGGER.info("Fetching items for the current month and year");

			List<ItemSummaryDTO> items = itemsRepository.findItemsByCurrentMonthAndYear();
			LOGGER.info("Retrieved {} items", items.size());

			items.sort(Comparator.comparing(ItemSummaryDTO::getUniqueId).reversed());
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
	 * Method to fetch all item request details. This retrieves all item requests
	 * regardless of their status and returns the details.
	 *
	 * @return a list of item requests
	 */
	public List<ItemsRequest> getItemRequestDetails() {
		LOGGER.info("Fetching all item request details.");
		return itemRequestRepo.findAll();

	}

	/**
	 * Method to fetch item request details for a specific user identified by their
	 * user ID. This retrieves all requests made by the user, filtering by their
	 * unique user ID.
	 *
	 * @param userId the unique ID of the user
	 * @return a list of item requests for the specified user
	 */
	public List<ItemsRequest> getItemRequestDetailsByUserId(int userId) {
		LOGGER.info("Fetching item request details for user ID: {}", userId);

		return itemRequestRepo.findByUser_UserId(userId);
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
	public Map<String, List<ItemSummaryDTO>> getItems() {
		LOGGER.info("Entering getAllItemsGroupedByDate method.");
		Map<String, List<ItemSummaryDTO>> groupedItems = new HashMap<>();
		try {
			// Fetch items from the repository
			List<ItemSummaryDTO> items = itemsRepository.findItemsSummary();

			// Group items by the date part of the uniqueId with "date:" prefix
			groupedItems = items.stream()
					.collect(Collectors.groupingBy(item -> "date:" + item.getUniqueId().split("-")[0]));

		} catch (Exception e) {
			LOGGER.error("Error in getAllItemsGroupedByDate: ", e);
		}
		return groupedItems;
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
		boolean exists = lookUpRepository.existsByNameIgnoreCase(term);
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
			itemDetails.put("dominantColor", item.getDominantColor());
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

//	/**
//	 * Method to fetch the category name by category ID.
//	 * It retrieves the category name associated with the given category ID from the repository.
//	 *
//	 * @param categoryId the ID of the category
//	 * @return the name of the category, or 'Unknown Category' if not found
//	 */
	private String getCategoryNameById(int categoryId) {
		LOGGER.info("Fetching category name for categoryId: {}", categoryId);

		LookUp lookUp = lookUpRepository.findById(categoryId).orElse(null);
		if (lookUp != null) {
			LOGGER.info("Found category: {}", lookUp.getName());
			return lookUp.getName();
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
	public Map<String, Object> getCarbonWeight() {
		LOGGER.info("Starting category stats calculation.");

		Map<String, Object> response = new HashMap<>();
		Map<String, Double> categoryWeights = new HashMap<>();
		Map<String, Integer> categoryItemCount = new HashMap<>();

		List<LookUp> allCategories = lookUpRepository.findAllByCode("C");
		LOGGER.info("Found {} categories.", allCategories);

		for (LookUp lookUp : allCategories) {
			System.out.println(lookUp);
			String categoryName = lookUp.getName();
			categoryWeights.put(categoryName, 0.0);
			categoryItemCount.put(categoryName, 0);
		}

		List<Items> unclaimedItems = itemsRepository.findByStatus(ItemStatus.UNCLAIMED);
		LOGGER.info("Found {} unclaimed items.", unclaimedItems.size());

		for (Items item : unclaimedItems) {
			int categoryId = item.getCategoryId();
//			LookUp lookUp = lookUpRepository.findAllById(categoryId).orElse(null);
			Optional<LookUp> lookUp = lookUpRepository.findById(categoryId);
			System.out.println();

			if (lookUp != null) {
				Double weight = 0.0;
				try {
					weight = Double.parseDouble(item.getCarbonWeight().replace(" kg", "").trim());
				} catch (NumberFormatException e) {
					weight = 0.0;
				}

				String categoryName = lookUp.get().getName();
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

	/**
	 * Method to fetch details of all items with the status 'OPEN'. It retrieves all
	 * items with the specified status and returns the count and details in the
	 * response.
	 *
	 * @return a map containing the item count, item details, and a success message
	 */
	public Map<String, Object> getItemDetails() {
		LOGGER.info("Fetching item details with status 'OPEN'");

		Map<String, Object> res = new HashMap<>();
		res.put(Constants.SUCCESS, false);
		res.put(Constants.MESSAGE, Constants.INVALID_INPUTS);
		List<String> statuses = Arrays.asList("UNCLAIMED");

		List<Items> filteredItems = itemsRepository.findByStatusIn(statuses);

		long count = filteredItems.size();
		Map<String, Object> response = new HashMap<>();
		response.put("count", count);
		response.put("items", filteredItems);
		response.put("message", "Successfully fetched the item details.");
		LOGGER.info("Fetched {} item(s) with status 'OPEN'", count);

		return response;
	}

}