package com.mss.demo.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mss.demo.entity.ClaimHistory;
import com.mss.demo.entity.ItemsRequest;
import com.mss.demo.entity.User;
import com.mss.demo.service.CloudVisionService;

@RestController
@RequestMapping("/api")
public class FoundObjectController {

	@Autowired
	private CloudVisionService cloudVisionService;

	/**
	 * Endpoint to upload a found object and detect labels using Cloud Vision.
	 *
	 * @param file The uploaded file as a MultipartFile.
	 * @return ResponseEntity containing the success message and detected labels.
	 * @throws IOException If an error occurs during file processing.
	 */
	@PostMapping("/upload")
	public ResponseEntity<String> uploadFoundObject(@RequestParam("image") MultipartFile file) throws IOException {
		String labels = cloudVisionService.detectLabels(file);
		System.out.println("Detected labels: " + labels);
		return ResponseEntity.ok("Object uploaded successfully. Labels detected: " + labels);
	}

	/**
	 * Endpoint to register an admin user.
	 *
	 * @param admin The admin login details.
	 * @return A success message.
	 */

//	@PostMapping("/login")
//	public String registerAdmin(@RequestBody Login admin) {
//		return cloudVisionService.registerAdmin(admin);
//	}
//	
	/**
	 * Endpoint to register a regular user.
	 *
	 * @param user The user registration details.
	 * @return A success message.
	 */
	@PostMapping("/register")
	public String registerUser(@RequestBody User user) {
		return cloudVisionService.registerUser(user);
	}

	/**
	 * Endpoint to send a push notification.
	 *
	 * @param toToken The recipient's device token.
	 * @param title   The title of the notification.
	 * @param body    The body of the notification.
	 * @return A success message.
	 */
	@PostMapping("/send")
	public ResponseEntity<String> sendNotification(@RequestParam String userId, @RequestParam String title,
			@RequestParam String body) {
		// Fetch device token from the database based on userId
		String deviceToken = getDeviceTokenFromDatabase(userId);

		if (deviceToken == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device token not found");
		}

		// Call the service class to send the notification
//		pushNotificationService.sendPushNotification(deviceToken, title, body);

		return ResponseEntity.ok("Notification sent successfully!");
	}

	private String getDeviceTokenFromDatabase(String userId) {
		// Logic to fetch the device token from the database based on userId
		return "user_device_token_from_db";
	}


	/**
	 * Endpoint to submit an item request.
	 *
	 * @param itemRequest The details of the requested item.
	 * @return A success message.
	 */
	@PostMapping("/itemRequest")
	public String requestItem(@RequestBody ItemsRequest itemRequest) {
		return cloudVisionService.requestItem(itemRequest);
	}

	/**
	 * Endpoint to save claim history.
	 *
	 * @param claimHistory The claim history details.
	 * @return The saved claim history.
	 */

	@PostMapping("/history")
	public ClaimHistory requestHistory(@RequestBody ClaimHistory claimHistory) {
		return cloudVisionService.saveRequestHistory(claimHistory);

	}

	/**
	 * Endpoint to retrieve user details by user ID.
	 *
	 * @param userId The ID of the user.
	 * @return The details of the user.
	 */

	@GetMapping("/userDetails/{userId}")
	public User getUserDetails(@PathVariable int userId) {
		return cloudVisionService.getUserDetails(userId);
	}

	/**
	 * Endpoint to retrieve claim history by item ID.
	 *
	 * @param itemId The ID of the item.
	 * @return An optional containing the claim history for the item.
	 */
	@GetMapping("/historyItem/{itemId}")
	public Optional<ClaimHistory> getClaimHistoryByItem(@PathVariable int itemId) {
		return cloudVisionService.getClaimHistoryByItem(itemId);
	}

	/**
	 * Endpoint to retrieve claim history by user ID.
	 *
	 * @param userId The ID of the user.
	 * @return A list of claim histories associated with the user.
	 */
	@GetMapping("/history/{userId}")
	public List<ClaimHistory> getClaimHistoryByUser(@PathVariable int userId) {
		return cloudVisionService.getClaimHistoryByUser(userId);
	}

	/**
	 * Endpoint to retrieve all item requests.
	 *
	 * @return A list of item request details.
	 */
	@GetMapping("/itemrequest")
	public List<ItemsRequest> getItemRequestDetails() {
		return cloudVisionService.getItemRequestDetails();
	}
}
