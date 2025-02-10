package com.claimit.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;

import com.claimit.constants.ClaimConstants;
import com.claimit.constants.Constants;
import com.claimit.dto.ItemDTO;
import com.claimit.dto.ItemStatus;
import com.claimit.dto.ItemStatusCountDTO;
import com.claimit.dto.ItemsSearchDTO;
import com.claimit.entity.Items;
import com.claimit.entity.error.ErrorDetails;
import com.claimit.service.ItemsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/items")
public class ItemsController {

	@Autowired
	private ItemsService itemsService;

	public ItemsController(ItemsService itemsService) {
		this.itemsService = itemsService;
	}

	/**
	 * Handles the upload of an image of a found object and detects labels using
	 * image processing.
	 *
	 * This method allows users to upload an image of a found object. It utilizes
	 * image recognition to detect labels that describe the object. Optionally, an
	 * organization ID can be provided for context, and users can edit the detected
	 * labels before saving.
	 *
	 * @param file         The image file of the found object to be uploaded.
	 * @param orgId        (Optional) The organization ID associated with the
	 *                     upload, if applicable.
	 * @param editedLabels (Optional) A list of manually edited labels provided by
	 *                     the user.
	 * @return A ResponseEntity containing a map of detected or edited labels.
	 * @throws IOException If an error occurs while processing the image.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "upload image", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> uploadFoundObject(@RequestParam("image") MultipartFile file,
			@RequestParam(value = "orgId", required = false) String orgId,
			@RequestParam(value = "editedLabels", required = false) List<String> editedLabels) throws IOException {
		Map<String, Object> labels = itemsService.detectLabels(file, orgId, editedLabels);
		return ResponseEntity.ok(labels);
	}

	/**
	 * Uploads an image for preview and performs label detection.
	 *
	 * This method allows users to upload an image for preview. It optionally
	 * accepts an organization ID, and processes the image to detect relevant
	 * labels.
	 *
	 * @param file  The image file to be uploaded.
	 * @param orgId (Optional) The organization ID associated with the image, if
	 *              applicable.
	 * @return A ResponseEntity containing the detected labels.
	 * @throws IOException If an error occurs while processing the image.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "upload image For Preview", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/image")
	public ResponseEntity<Map<String, Object>> uploadImageForPreview(@RequestParam("image") MultipartFile file,
			@RequestParam(required = false) String orgId) throws IOException {
		Map<String, Object> labels = itemsService.uploadImageForPreview(file, orgId);
		return ResponseEntity.ok(labels);
	}

	/**
	 * Approves or rejects a request based on the provided item ID and status. This
	 * method allows updating the status of an item, either approving or rejecting
	 * it. If rejected, an optional reason can be provided.
	 * 
	 * @param itemId         The ID of the item to approve or reject.
	 * @param status         The status to set for the item (approved or rejected).
	 * @param rejectedReason (Optional) The reason for rejecting the request.
	 * @return A map containing the updated status information.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PutMapping("/approved-rejected")
	public Map<String, Object> approveOrRejectRequest(@RequestParam int itemId, @RequestParam ItemStatus status,
			@RequestParam(required = false) String rejectedReason) {
		return itemsService.approveOrRejectRequest(itemId, status, rejectedReason);
	}

	/**
	 * Retrieves the item status counts for a specific month. This method returns
	 * the count of items categorized by their status for a given month.
	 * 
	 * @param month The month for which the status counts are requested.
	 * @return A ResponseEntity containing a list of ItemStatusCountDTO representing
	 *         the counts for each status.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieve the count of items grouped by status for a specified month ", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/statistics")
	public ResponseEntity<List<ItemStatusCountDTO>> getItemStatusCountsByMonth(@RequestParam String month) {
		List<ItemStatusCountDTO> result = itemsService.getItemStatusCounts(month);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * Retrieves the count of items by category for a specific month. This method
	 * returns the count of items categorized by their type for a given month.
	 * 
	 * @param month The month for which the item count is requested.
	 * @return A ResponseEntity containing the item counts by category.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieves the count of items by category for a specified month", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/categoriesCount")
	public ResponseEntity<List<Map<String, Object>>> getItemCountByCategoryForSpecificMonth(
			@RequestParam String month) {
		List<Map<String, Object>> itemCounts = itemsService.getItemCountByCategoryForSpecificMonth(month);

		if (itemCounts.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(List.of(Map.of("message", "No data found for the specified month.")));
		}

		return ResponseEntity.ok(itemCounts);
	}

	/**
	 * Retrieves a list of items grouped by their date of creation. This method
	 * returns items organized by their respective dates, enabling easy viewing of
	 * items added over time.
	 * 
	 * @return A ResponseEntity containing a map of items grouped by date.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "fetching items based on dates", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/grouped")
	public ResponseEntity<Map<String, List<ItemDTO>>> getItemsGroupedByDate() {
		Map<String, List<ItemDTO>> items = itemsService.getItemsGroupedByDate();
		return ResponseEntity.ok(items);
	}

	/**
	 * Retrieves the carbon weight data. This method returns the calculated carbon
	 * weight data associated with the items or their processes.
	 * 
	 * @return A map containing the carbon weight information.
	 */
//	@ResponseStatus(HttpStatus.OK)
//	@Operation(summary = "retrieves and returns category statistics", responses = {
//			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
//			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
//			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
//			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
//			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
//			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
//	@GetMapping("/carbonWeight")
//	public Map<String, Object> getCarbonWeight() {
//		return itemsService.getCarbonWeight();
//	}

	/**
	 * Archives expired items based on their ID or all expired items if no ID is
	 * provided.
	 * 
	 * This method archives items that have expired. It can archive specific items
	 * if an ID is provided, or all expired items if no ID is provided.
	 * 
	 *
	 * @param itemId (Optional) The ID of the item to archive. If not provided, all
	 *               expired items are archived.
	 * @return A map containing the response after archiving the items.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "update the status of the items", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/archive-expired-items")
	public String archiveExpiredItems() {
		itemsService.archiveExpiredItemsAutomatically();
		return "Expired items have been archived successfully.";
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "update the status of the items", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PutMapping("/archiveExpired")
	public ResponseEntity<Map<String, Object>> archiveExpiredItems(@RequestParam("fromDate") String fromDateStr,
			@RequestParam("toDate") String toDateStr,
			@RequestParam(value = "expirationDate", required = false) String expirationDateStr) {
		Map<String, Object> response = itemsService.archiveExpiredItems(fromDateStr, toDateStr, expirationDateStr);
		return ResponseEntity.ok(response);
	}

	/**
	 * Retrieves all items stored in the system.
	 *
	 * 
	 * This method returns all items currently available in the database.
	 * 
	 *
	 * @return A map containing a list of all items.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieve all items", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/listOfItems")
	public Map<String, Object> getItems() {
		return itemsService.getAllItems();
	}

	/**
	 * Searches for items based on the provided search criteria.
	 *
	 * 
	 * This method allows searching for items by different criteria such as email,
	 * date, status, or query. It also supports image-based search when an image
	 * file is provided.
	 * 
	 *
	 * @param searchType   The type of search (EMAIL, QUERY, IMAGE).
	 * @param email        (Optional) The email address for filtering items.
	 * @param receivedDate (Optional) The received date for filtering items.
	 * @param status       (Optional) The status for filtering items.
	 * @param query        (Optional) The search query for searching items by name
	 *                     or other attributes.
	 * @param file         (Optional) The image file for searching items by image.
	 * @return A {@link ResponseEntity} containing the search results or an error
	 *         message.
	 * @throws IllegalArgumentException If an invalid search type is provided.
	 */

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieve all items", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })

	@PostMapping("/search")
	public ResponseEntity<Map<String, Object>> searchItems(@RequestParam String searchType,
			@RequestParam(required = false) String email, @RequestParam(required = false) String receivedDate,
			@RequestParam(required = false) String status, @RequestParam(required = false) String query,
			@RequestParam(required = false) MultipartFile file) {

		Map<String, Object> response = new HashMap<>();

		try {
			switch (searchType.toUpperCase()) {
			case "EMAIL":
				response = itemsService.searchItems(email, receivedDate, status);
				break;

			case "QUERY":
				List<ItemsSearchDTO> searchResults = itemsService.searchItems(query);
				response.put("items", searchResults);
				response.put(Constants.SUCCESS, true);
				break;

			case "IMAGE":
				if (file == null || file.isEmpty()) {
					throw new IllegalArgumentException("Image file cannot be empty.");
				}
				response = itemsService.uploadImageForSearch(file);
				break;

			default:
				throw new IllegalArgumentException("Invalid searchType provided: " + searchType);
			}

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			response.put(Constants.SUCCESS, false);
			response.put(Constants.MESSAGE, e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
	}

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "retrieve all archived items", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/archived")
	public ResponseEntity<List<Items>> getArchivedItems(
			@RequestParam(value = "fromDate", required = false) String fromDateStr,
			@RequestParam(value = "toDate", required = false) String toDateStr) {
		List<Items> items = itemsService.getArchivedItems(fromDateStr, toDateStr);
		return ResponseEntity.ok(items);
	}

	@PutMapping("/archive-expired")
	public ResponseEntity<Map<String, Object>> archiveExpiredItems(@RequestParam(required = false) Integer itemId) {
		Map<String, Object> response = itemsService.archiveExpiredItems(itemId);
		return ResponseEntity.ok(response);
	}

}
