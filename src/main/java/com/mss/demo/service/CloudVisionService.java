package com.mss.demo.service;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.DominantColorsAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.protobuf.ByteString;
import com.mss.demo.dto.ColorUtils;
import com.mss.demo.entity.ClaimHistory;
import com.mss.demo.entity.Items;
import com.mss.demo.entity.ItemsRequest;
import com.mss.demo.entity.Login;
import com.mss.demo.entity.User;
import com.mss.demo.repo.ClaimHistoryRepository;
import com.mss.demo.repo.ItemRequestRepo;
import com.mss.demo.repo.ItemsRepo;

import com.mss.demo.repo.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class CloudVisionService {

	@Autowired
	private ItemsRepo itemsRepository;

	@Autowired
	private ItemRequestRepo itemRequestRepo;

	@Autowired
	private ClaimHistoryRepository claimHistoryRepository;

	@Autowired
	private UserRepo userRepository;

	/**
	 * Detects labels, dominant color, and visible text in an uploaded image.
	 *
	 * @param file The uploaded image file.
	 * @return A description of the detected labels, dominant color, and text.
	 * @throws IOException If an error occurs while processing the image.
	 */

	public String detectLabels(MultipartFile file) throws IOException {
		ByteString imgBytes = ByteString.readFrom(file.getInputStream());
		Image img = Image.newBuilder().setContent(imgBytes).build();

		List<Feature> features = List.of(Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION).build(),
				Feature.newBuilder().setType(Feature.Type.IMAGE_PROPERTIES).build(),
				Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build());

		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().setImage(img).addAllFeatures(features).build();
		StringBuilder description = new StringBuilder();

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			AnnotateImageResponse response = client.batchAnnotateImages(List.of(request)).getResponsesList().get(0);

			Items item = new Items();
			item.setImage(file.getBytes()); // Store image data

			String objectName = response.getLocalizedObjectAnnotationsList().stream().findFirst()
					.map(LocalizedObjectAnnotation::getName).orElse("Unknown Object");
			item.setItemName(objectName);
			description.append("Object: ").append(objectName).append("\n");

			// Get the dominant color
			if (response.hasImagePropertiesAnnotation()) {
				DominantColorsAnnotation colors = response.getImagePropertiesAnnotation().getDominantColors();

				// Find the color with the highest score
				String mostDominantColor = colors.getColorsList().stream()
						.max((colorInfo1, colorInfo2) -> Float.compare(colorInfo1.getScore(), colorInfo2.getScore()))
						.map(colorInfo -> {
							int red = (int) colorInfo.getColor().getRed();
							int green = (int) colorInfo.getColor().getGreen();
							int blue = (int) colorInfo.getColor().getBlue();

							if (green > red && green > blue) {
								return "Green"; // Return "Green" if the green component is dominant
							}

							// Default: return closest color name
							return ColorUtils.getClosestColorName(red, green, blue);
						}).orElse("Unknown");

				item.setDominantColor(mostDominantColor);
				description.append("Dominant Color: ").append(mostDominantColor).append("\n");
			}

			// Text Detection - Extract any visible text
			List<String> texts = response.getTextAnnotationsList().stream()
					.map(annotation -> annotation.getDescription()).collect(Collectors.toList());
			String detectedText = texts.isEmpty() ? "None" : String.join(", ", texts);
			item.setDetectedText(detectedText);
			description.append("Text Detected: ").append(detectedText).append("\n");

			itemsRepository.save(item);

			return description.toString();
		}
	}

	/**
	 * Registers a new user in the system.
	 *
	 * @param user The user details.
	 * @return A success message.
	 */

	public String registerUser(User user) {

		if (user.getRole() == 0) {
			return "Access denied";
		}
		userRepository.save(user);
		return "User registered successfully!";

	}

	/**
	 * Saves the claim history for a user.
	 *
	 * @param claimHistory The claim history details.
	 * @return The saved ClaimHistory entity.
	 */

	public ClaimHistory saveRequestHistory(ClaimHistory claimHistory) {

		Optional<User> userOpt = userRepository.findById(claimHistory.getUser().getUserId());
		if (userOpt.isEmpty()) {
			throw new RuntimeException("User with ID " + claimHistory.getUser().getUserId() + " not found.");
		}
		claimHistory.setUser(userOpt.get());

		Optional<ItemsRequest> requestOpt = itemRequestRepo.findById(claimHistory.getRequest().getRequestId());
		if (requestOpt.isEmpty()) {
			throw new RuntimeException("Request with ID " + claimHistory.getRequest().getRequestId() + " not found.");
		}
		claimHistory.setRequest(requestOpt.get());

		if (claimHistory.getClaimDate() == null) {
			claimHistory.setClaimDate(LocalDateTime.now());
		}

		if (claimHistory.getVerificationDate() == null) {
			claimHistory.setVerificationDate(LocalDateTime.now());
		}

		return claimHistoryRepository.save(claimHistory);
	}

	/**
	 * Retrieves user details by user ID.
	 *
	 * @param userId The ID of the user.
	 * @return The User entity if found, or null if not found.
	 */
	public List<User> getUserDetails(String searchTerm, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<User> usersPage;

		if (searchTerm == null || searchTerm.isEmpty()) {
			usersPage = userRepository.findAll(pageable);
		} else {
			usersPage = userRepository.findByNameContainingIgnoreCase(searchTerm, pageable);
		}

		return usersPage.getContent();
	}

	/**
	 * Processes an item request, linking the request to the user and item.
	 *
	 * @param itemRequest The details of the item request.
	 * @return A success message if the request is processed successfully.
	 */
	public String requestItem(ItemsRequest itemRequest) {
		System.out.println(itemRequest.getUser());
		// Log the userId and itemId
		System.out.println("User ID: " + itemRequest.getUser().getUserId());
		System.out.println("Item ID: " + itemRequest.getItem().getItemId());

		// Fetch the existing User from the database
		Optional<User> userOpt = userRepository.findById(itemRequest.getUser().getUserId());
		if (userOpt.isEmpty()) {
			System.out.println("User not found with ID: " + itemRequest.getUser().getUserId());
			throw new RuntimeException("User not found");
		}
		User user = userOpt.get();

		// Fetch the Item from the database
		Optional<Items> itemOpt = itemsRepository.findById(itemRequest.getItem().getItemId());
		if (itemOpt.isEmpty()) {
			System.out.println("Item not found with ID: " + itemRequest.getItem().getItemId());
			throw new RuntimeException("Item not found");
		}
		Items item = itemOpt.get();

		// Set the fetched User and Item in the ItemsRequest
		itemRequest.setUser(user);
		itemRequest.setItem(item);

		if (itemRequest.getClaimedDate() == null) {
			itemRequest.setClaimedDate(LocalDateTime.now());
		}
		// Save the ItemsRequest to the database
		itemRequestRepo.save(itemRequest);

		return "Item requested successfully!";
	}

	/**
	 * Retrieves claim history for a specific user by their ID.
	 *
	 * @param userId The ID of the user.
	 * @return A list of ClaimHistory associated with the user.
	 */
	public List<ClaimHistory> getClaimHistoryByUser(int userId) {
		return claimHistoryRepository.findByUserId(userId);
	}

	/**
	 * Retrieves claim history for a specific item by its ID.
	 *
	 * @param itemId The ID of the item.
	 * @return An Optional containing the ClaimHistory if found.
	 */

	public Optional<ClaimHistory> getClaimHistoryByItem(int itemId) {
		return claimHistoryRepository.findById(itemId);
	}

	/**
	 * Retrieves all item request details.
	 *
	 * @return A list of all item requests stored in the repository.
	 */
	public List<ItemsRequest> getItemRequestDetails() {
		return itemRequestRepo.findAll();
	}

	public String getLoginDetails(Login login) {
		String emailId = login.getEmail();
		String password = login.getPassword();

		List<User> users = userRepository.findByEmailAndRole(emailId, login.getRole());

		if (users.isEmpty()) {
			return "Credentials not found";
		} else {
			User user = users.get(0);
			if (user.getPassword().equals(password)) {
				if (user.getRole() == 0) {
					return "Admin Login successfully";
				} else if (user.getRole() == 1) {
					return "User Login successfully";
				} else {
					return "Access denied";
				}
			} else {
				return "Incorrect password";
			}
		}
	}

	public List<Items> getAllItems() {
		return itemsRepository.findAll();
	}

	public List<Items> getItemDetails(String searchTerm, int page, int size) {
	    Pageable pageable = PageRequest.of(page, size);  
	    Page<Items> itemsPage;

	    if (searchTerm == null || searchTerm.isEmpty()) {
	        itemsPage = itemsRepository.findAll(pageable);
	    } else {
	        itemsPage = itemsRepository.findByItemNameContainingIgnoreCase(searchTerm, pageable);
	    }
	    return itemsPage.getContent();
	}

}
