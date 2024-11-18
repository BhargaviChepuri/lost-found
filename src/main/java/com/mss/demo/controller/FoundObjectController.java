package com.mss.demo.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mss.demo.entity.ClaimHistory;
import com.mss.demo.entity.Items;
import com.mss.demo.entity.ItemsRequest;
import com.mss.demo.entity.Login;
import com.mss.demo.entity.User;
import com.mss.demo.repo.UserRepo;
import com.mss.demo.service.CloudVisionService;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class FoundObjectController {

	@Autowired
	private CloudVisionService cloudVisionService;
	
	@Autowired
	private UserRepo userRepository;

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
	 * @return The details of the user.
	 */

    @GetMapping("/users")
    public List<User> getUserDetails(
        @RequestParam(required = false) String searchTerm, // Search term for username
        @RequestParam(defaultValue = "0") int page, // Default to page 0 if not provided
        @RequestParam(defaultValue = "10") int size // Default to page size of 10 if not provided
    ) {
        return cloudVisionService.getUserDetails(searchTerm, page, size);
    }

    @GetMapping("/userDetails")
     public List<User> getDetails(){
    	return userRepository.findAll();
    }
    
	@GetMapping("/login")
	public String getLoginDetails(@RequestBody Login login) {
		return cloudVisionService.getLoginDetails(login);
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

	 @GetMapping("/itemsby")
	    public List<Items> getItemDetails(
	        @RequestParam(required = false) String searchTerm, 
	        @RequestParam(defaultValue = "0") int page, 
	        @RequestParam(defaultValue = "10") int size
	    ) {
	        return cloudVisionService.getItemDetails(searchTerm, page, size);
	    }
	
	@GetMapping("/items")
	public List<Items> getItems() {
		return cloudVisionService.getAllItems();
	}
}
