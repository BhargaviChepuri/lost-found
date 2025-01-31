package com.claimit.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.claimit.constants.ClaimConstants;
import com.claimit.dto.ItemStatus;
import com.claimit.entity.ClaimHistory;
import com.claimit.entity.User;
import com.claimit.entity.error.ErrorDetails;
import com.claimit.service.ClaimService;
import com.claimit.service.ItemsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/claims")
public class ClaimController {
	
	@Autowired
	public ClaimService claimService;
	
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieves all claim notifications", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/notifications")
	public ResponseEntity<Map<String, Object>> getAllClaims() {
		Map<String, Object> response = claimService.getAllClaims();
		return ResponseEntity.ok(response);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "update a claim request", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PutMapping("/update-request")
	public ResponseEntity<Map<String, Object>> updateClaimRequest(@RequestBody User user) {
		Map<String, Object> response = claimService.updateClaimRequest(user);
		return ResponseEntity.ok(response);
	}

	
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "insert history data", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ClaimService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/history")
	public Map<String, Object> saveRequestHistory(@RequestBody ClaimHistory claimHistory) {
		return claimService.saveRequestHistory(claimHistory);

	}


	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "insert claim data", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ClaimService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/popup-claim")
	public Map<String, Object> claimItem(@RequestBody Map<String, Object> requestData) {
	    return claimService.claimItem(requestData);
	}


	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieves the claim history for a user based on their email", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ClaimService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/getHistory")
	public Map<String, Object> getClaimHistoryByEmail(@RequestParam(required = false) String email) {
		return claimService.getClaimHistoryByEmail(email);
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "updates the claim status based on the provided claim ID and status", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ClaimService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PutMapping("/updateStatus")
	public ResponseEntity<Map<String, Object>> updateClaimStatus(@RequestParam int claimId,
			@RequestParam ItemStatus status) {
		Map<String, Object> response = claimService.updateClaimStatusAndNotify(claimId, status);
		return ResponseEntity.ok(response);
	}


}
