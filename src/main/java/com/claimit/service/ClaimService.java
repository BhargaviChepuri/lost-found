package com.claimit.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.claimit.constants.Constants;
import com.claimit.dto.ClaimHistoryDTO;
import com.claimit.dto.ClaimRequestDTO;
import com.claimit.dto.ItemStatus;
import com.claimit.dto.ItemsRequestDTO;
import com.claimit.entity.ClaimHistory;
import com.claimit.entity.ClaimRequest;
import com.claimit.entity.Items;
import com.claimit.entity.ItemsRequest;
import com.claimit.entity.User;
import com.claimit.exception.ResourceNotFoundException;
import com.claimit.repo.ClaimHistoryRepository;
import com.claimit.repo.ClaimRequestRepository;
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
	private ClaimRequestRepository claimRequestRepository;

	@Autowired
	private EmailService emailService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ClaimService.class);

	public Map<String, Object> getAllClaims() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<ClaimRequest> claims = claimRequestRepository.findAll();
			response.put("status", "success");
			response.put("data", claims);
		} catch (Exception e) {
			response.put("status", "error");
			response.put("message", "An error occurred: " + e.getMessage());
		}
		return response;
	}

	public Map<String, Object> updateClaimRequest(ClaimRequest claimRequest) {
		Map<String, Object> response = new HashMap<>();
		response.put(Constants.SUCCESS, false);
		response.put(Constants.MESSAGE, Constants.INVALID_INPUTS);
		boolean isRead = true;
		Optional<ClaimRequest> requestOpt = claimRequestRepository.findById(claimRequest.getId());
		if (requestOpt.isEmpty()) {
			response.put(Constants.MESSAGE, "Claim request not found.");
			return response;
		}
		ClaimRequest existingRequest = requestOpt.get();
		if (claimRequest.getName() != null) {
			existingRequest.setName(claimRequest.getName());
		}
		if (claimRequest.getMessage() != null) {
			existingRequest.setMessage(claimRequest.getMessage());
		}
		if (claimRequest.getEmail() != null) {
			existingRequest.setEmail(claimRequest.getEmail());
		}
		existingRequest.setRead(isRead);
		ClaimRequest updatedRequest = claimRequestRepository.save(existingRequest);
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

	/**
	 * Submits a claim request and saves it to the repository. 1. Logs the starting
	 * information for the claim request submission. 2. Creates a new `ClaimRequest`
	 * object and sets its properties. 3. Saves the claim request to the repository.
	 * 4. Returns the saved claim request object.
	 *
	 * @param claimRequest The claim request details to be submitted.
	 * @return The saved claim request object.
	 * @throws IOException if there is an issue during the claim request submission.
	 */
	public ClaimRequest submitRequest(ClaimRequest claimRequest) throws IOException {
		LOGGER.info("Starting to submit claim request for: {}", claimRequest.getName());

		ClaimRequest claimRequest1 = new ClaimRequest();

		claimRequest1.setName(claimRequest.getName());
		claimRequest1.setEmail(claimRequest.getEmail());
		claimRequest1.setMessage(claimRequest.getMessage());
		claimRequest1.setRead(false);

		return claimRequestRepository.save(claimRequest);
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
	

}
