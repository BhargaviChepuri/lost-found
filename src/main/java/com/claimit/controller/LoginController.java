package com.claimit.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.claimit.constants.ClaimConstants;
import com.claimit.dto.Login;
import com.claimit.entity.error.ErrorDetails;
import com.claimit.service.ItemsService;
import com.claimit.service.LoginService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
public class LoginController {
	
	@Autowired
	public LoginService loginService;
	
	public LoginController(LoginService loginService) {
		this.loginService = loginService;
	}

	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	/**
	 * Logs in a user with provided credentials.
	 *
	 * <p>This method allows users to log in by providing their email and password. It processes the login
	 * request and returns the appropriate response based on the validation results.</p>
	 *
	 * @param login The login credentials including email and password.
	 * @return A {@link ResponseEntity} containing a map with the login response data, which includes
	 *         authentication status and potential error messages.
	 */
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "login with credentials", responses = {
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_200, description = ClaimConstants.RESPONSE_CODE_200_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ItemsService.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_204, description = ClaimConstants.RESPONSE_CODE_204_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_400, description = ClaimConstants.RESPONSE_CODE_400_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_422, description = ClaimConstants.RESPONSE_CODE_422_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_429, description = ClaimConstants.RESPONSE_CODE_429_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))),
			@ApiResponse(responseCode = ClaimConstants.RESPONSE_CODE_503, description = ClaimConstants.RESPONSE_CODE_503_DESCRIPTION, content = @Content(mediaType = ClaimConstants.MEDIA_TYPE, schema = @Schema(implementation = ErrorDetails.class))) })
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody Login login) {
		LOGGER.info("Login request received for email: {}", login.getEmail());
		return loginService.login(login);
	}
}
