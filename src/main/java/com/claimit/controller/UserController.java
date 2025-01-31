package com.claimit.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.claimit.constants.ClaimConstants;
import com.claimit.entity.error.ErrorDetails;
import com.claimit.service.EmailService;
import com.claimit.service.ItemsService;
import com.claimit.service.NotificationService;
import com.claimit.service.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private EmailService emailService;

	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "sends notifications to users before item expiration and saves the notification details.", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@GetMapping("/expiryNotification")
	public String notifyUsersBeforeExpiration() {
		notificationService.notifyAndSaveItem();
		return "Notifications sent successfully";

	}

	@PostMapping("/send-email")
	public String sendClaimNotification(@RequestParam String email, @RequestParam String uniqueId,
			@RequestParam String itemName, @RequestParam String itemStatus) {

		emailService.sendEmailWithQRCode(email, uniqueId, itemName, itemStatus);
		return "Email with QR code sent successfully!";
	}

	@GetMapping(value = "/generate-qr-base64")
	public String generateQRCodeBase64(@RequestParam String uniqueId, @RequestParam String itemName,
			@RequestParam String itemStatus) {
		try {
			String qrContent = "{\"uniqueId\":\"" + uniqueId + "\",\"itemName\":\"" + itemName + "\",\"itemStatus\":\""
					+ itemStatus + "\"}";

			BitMatrix bitMatrix = new MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageIO.write(qrImage, "png", outputStream);

			String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

			return "{\"qrCode\": \"data:image/png;base64," + base64Image + "\"}"; // Return JSON
		} catch (Exception e) {
			return "{\"error\": \"QR Code generation failed\"}";
		}
	}

}
