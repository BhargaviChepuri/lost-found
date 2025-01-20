package com.claimit.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.claimit.constants.ClaimConstants;
import com.claimit.dto.CategoriesDTO;
import com.claimit.dto.ClaimRequestDTO;
import com.claimit.dto.ItemStatus;
import com.claimit.dto.ItemStatusCountDTO;
import com.claimit.entity.ClaimHistory;
import com.claimit.entity.ClaimRequest;
import com.claimit.entity.Items;
import com.claimit.entity.ItemsRequest;
import com.claimit.entity.Login;
import com.claimit.entity.Organisation;
import com.claimit.entity.Subcategories;
import com.claimit.entity.User;
import com.claimit.entity.error.ErrorDetails;
import com.claimit.repo.UserRepo;
import com.claimit.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	private AdminService adminService;

	@Autowired
	private UserRepo userRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

	/* * @param login The Login object containing the email and password provided by the administrator.
	 * @return A ResponseEntity containing a map with the result of the authentication attempt, including success or error details.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody Login login) {
		LOGGER.info("Login request received for email: {}", login.getEmail());
		return adminService.authenticate(login);
	}

	/* * This method allows users to upload an image of a found object along with the associated organization ID (`orgId`). It processes the image to detect labels (such as object categories, tags, or descriptive terms) using the `detectLabels()` function from the `adminService`. The labels are then returned as part of the response to assist with categorizing the found object.
    * 
    * @param file The `MultipartFile` containing the image of the found object uploaded by the user.
    * @param orgId The `orgId` representing the organization to which the found object is associated.
    * @return A `ResponseEntity` containing a map with detected labels for the uploaded image.
    */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "upload image", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> uploadFoundObject(@RequestParam("image") MultipartFile file,
			@RequestParam("orgId") String orgId) throws IOException {
		Map<String, Object> labels = adminService.detectLabels(file, orgId);
		return ResponseEntity.ok(labels);
	}


	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "insert history data", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/history")
	public Map<String, Object> requestHistory(@RequestBody ClaimHistory claimHistory) {
		return adminService.saveRequestHistory(claimHistory);

	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "insert the organisation data", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/saveOrganisation")
	public Map<String, Object> saveOrganisation(@RequestBody Organisation organisation) {
		return adminService.saveOrganisation(organisation);

	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "update the items by using itemId ", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PutMapping("/updateitemsimage/{itemid}")
	public ResponseEntity<String> updateItemsImage(@RequestParam("image") MultipartFile image,
			@PathVariable("itemid") int itemId) {
		try {
			String response = adminService.updateItemsImage(image.getBytes(), itemId);
			return ResponseEntity.ok(response);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process the image.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Get all organisations by orgID", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/byOrgId")
	public List<Items> getItemsByOrgId(@RequestParam("orgId") String orgId) {
		return adminService.getItemByOrgId(orgId);

	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "To retrieve user details by searching.", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/users")
	public List<User> getUserDetails(@RequestParam(required = false) String searchTerm,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return adminService.getUserDetails(searchTerm, page, size);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "To retrieve user details.", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/userDetails")
	public List<User> getDetails() {
		return userRepository.findAll();
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "get all items", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/listOfItems")
	public Map<String, Object> getItems() {
		return adminService.getAllItems();
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "get all categories", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/getcategories")
	public List<CategoriesDTO> getCategories() {
		return adminService.getCategories();
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "get all subCategories", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/getsubcategories")
	public List<Subcategories> getSubcategories() {
		return adminService.getSubcategories();
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "get all organisations", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })

	@GetMapping("/listOfOrganisation")
	public List<Organisation> listOfOrganisation() {
		return adminService.listOfOrganisation();

	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "delete Items", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@DeleteMapping("/deleteItems")
	public String deleteItems(@RequestBody List<Integer> itemIds) {
		adminService.deleteItems(itemIds);
		return "Items deleted successfully";
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "insert claim data", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/popup-claim")
	public Map<String, Object> claimItem(@RequestBody ClaimRequestDTO claimRequest) {
		return adminService.claimItem(claimRequest);

	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "update the status of the items", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PutMapping("/archive-expired-items")
	public Map<String, Object> archiveExpiredItems(@RequestParam(required = false) Integer itemId) {
		return adminService.archiveExpiredItems(itemId);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "Retrieve items through search.", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/search")
	public Map<String, Object> searchItems(@RequestParam(required = false) String mail,
			@RequestParam(required = false) Date receivedDate, @RequestParam(required = false) String status)
			throws ParseException {

		return adminService.searchItems(mail, receivedDate, status);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "update the status", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })

	@PutMapping("/approved-rejected")
	public Map<String, Object> approveOrRejectRequest(@RequestParam int itemId, @RequestParam ItemStatus status,
			@RequestParam(required = false) String rejectedReason) {
		return adminService.approveOrRejectRequest(itemId, status, rejectedReason);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "get all requests", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/all")
	public ResponseEntity<List<ItemsRequest>> getAllRequests() {
		List<ItemsRequest> requests = adminService.getAllRequests();
		return new ResponseEntity<>(requests, HttpStatus.OK);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieve the count of items grouped by status for a specified month ", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/status/counts")
	public ResponseEntity<List<ItemStatusCountDTO>> getItemStatusCountsByMonth(@RequestParam String month) {
		List<ItemStatusCountDTO> result = adminService.getItemStatusCounts(month);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieves the count of items by category for a specified month", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/countByCategory")
	public ResponseEntity<List<Map<String, Object>>> getItemCountByCategoryForSpecificMonth(
			@RequestParam String month) {
		List<Map<String, Object>> itemCounts = adminService.getItemCountByCategoryForSpecificMonth(month);

		if (itemCounts.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(List.of(Map.of("message", "No data found for the specified month.")));
		}

		return ResponseEntity.ok(itemCounts);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieves all claim notifications", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/notifications")
	public ResponseEntity<Map<String, Object>> getAllClaims() {
		Map<String, Object> response = adminService.getAllClaims();
		return ResponseEntity.ok(response);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "update a claim request", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_401, description = ClaimConstants.RESPONSE_CODE_401_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_403, description = ClaimConstants.RESPONSE_CODE_403_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_500, description = ClaimConstants.RESPONSE_CODE_500_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PutMapping("/update-claimrequest")
	public ResponseEntity<Map<String, Object>> updateClaimRequest(@RequestBody ClaimRequest claimReques) {
		Map<String, Object> response = adminService.updateClaimRequest(claimReques);
		return ResponseEntity.ok(response);
	}

}
