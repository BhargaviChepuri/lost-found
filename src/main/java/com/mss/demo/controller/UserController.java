package com.mss.demo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.mss.demo.constants.ClaimConstants;
import com.mss.demo.dto.ItemStatus;
import com.mss.demo.dto.ItemsSearchDTO;

import com.mss.demo.entity.ItemsRequest;
import com.mss.demo.entity.ResponseMessage;
import com.mss.demo.entity.error.ErrorDetails;
import com.mss.demo.service.AdminService;
import com.mss.demo.service.NotificationService;
import com.mss.demo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private NotificationService notificationService;



	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/claim-history")
	public Map<String, Object> getClaimHistory(@RequestParam(required = false) String email,@RequestParam(required = false) String userName ) {
		
System.out.println("claim-history");
		if (email == null || email.trim().isEmpty()) {
			return userService.getClaimHistory();
		} else {
			return userService.getClaimHistoryByEmail(email,userName);
		}
	}
	
	
	

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/search")
	public ResponseEntity<List<ItemsSearchDTO>> searchItems(@RequestParam(required = false) String query) {
		List<ItemsSearchDTO> items = userService.searchItems(query);
		return ResponseEntity.ok(items);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/search-by-image")
	public ResponseEntity<Map<String, Object>> uploadImageForSearch(@RequestParam("image") MultipartFile file)
			throws IOException {

		Map<String, Object> response = userService.uploadImageForSearch(file);
		return ResponseEntity.ok(response);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PutMapping("/updateStatus")
	public ResponseEntity<Map<String, Object>> updateClaimStatus(@RequestParam int claimId,
			@RequestParam ItemStatus status) {
		Map<String, Object> response = userService.updateClaimStatusAndNotify(claimId, status);
		return ResponseEntity.ok(response);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/notify-expiration")
	public String notifyUsersBeforeExpiration() {

		notificationService.notifyAndSaveItem();
		return "Notifications sent successfully";

	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = AdminService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_404, description = ClaimConstants.RESPONSE_CODE_404_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/calculate")
	public Map<String, Object> getCategoryStats() {
		return userService.calculateCategoryStats();
	}


}
