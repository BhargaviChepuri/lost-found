package com.claimit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.claimit.constants.Constants;
import com.claimit.dto.ClaimHistoryDTO;
import com.claimit.dto.ItemStatus;
import com.claimit.dto.ItemsRequestDTO;
import com.claimit.entity.ClaimHistory;
import com.claimit.entity.Items;
import com.claimit.entity.ItemsRequest;
import com.claimit.entity.User;
import com.claimit.exception.ResourceNotFoundException;
import com.claimit.repo.ClaimHistoryRepository;
import com.claimit.repo.ItemRequestRepo;
import com.claimit.repo.ItemsRepo;
import com.claimit.repo.UserRepo;

@Service
public class ClaimService {

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
	private EmailService emailService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ClaimService.class);

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
					claimData.put("name", user.getUserName());
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

		if (user.getUserName() != null) {
			existingRequest.setUserName(user.getUserName());
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
			LocalDateTime claimDate;
			if (claimHistoryDTO.getClaimDate() != null) {
			    claimDate = claimHistoryDTO.getClaimDate();
			} else {
			    claimDate = LocalDateTime.now();
			}
			claimHistory.setClaimDate(claimDate);
			claimHistory.setUser(itemsRequest.getUser());
			claimHistory.setUserEmail(itemsRequest.getUser().getEmail()); 
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
		user.setUserName(name);
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
			userData.setUserName(user.getUserName());
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

		String message = "The item " + item.getTitle() + " has been claimed by " + item.getUser().getUserName();

		userData.setMessage(message);
		userRepository.save(userData);

		EmailService.sendClaimConfirmationEmail(user.getEmail(), item);
		emailService.sendClaimNotificationToAdmin(item);

		res.put(Constants.MESSAGE, "Successfully claimed the item.");
		res.put(Constants.SUCCESS, true);
		return res;
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
	public Map<String, Object> getClaimHistoryByEmail(String email) {
		LOGGER.info("Fetching claim history for email: {}", email);

		Map<String, Object> response = new HashMap<>();

		if (email == null || email.trim().isEmpty()) {
			LOGGER.warn("Email cannot be null or empty.");

			response.put(Constants.SUCCESS, false);
			response.put(Constants.MESSAGE, "Email cannot be null or empty.");
			response.put("claimHistory", List.of());
			response.put("itemRequests", List.of());
			return response;
		}

		Optional<User> optionalUser = userRepository.findByEmail(email);
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

}
