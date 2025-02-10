package com.claimit.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.claimit.dto.Login;

@Service
public class LoginService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemsService.class);

	private static final String ADMIN_EMAIL = "admin@mail.com";
	private static final String ADMIN_PASSWORD = "admin123";
	private static final boolean IS_ADMIN = true;

	/**
	 * Authenticates a user based on the provided email, password, and admin status.
	 * 1. Checks if the email and password are not null. 2. Compares the provided
	 * credentials with the predefined admin credentials. 3. If the credentials
	 * match the admin credentials, a success response is generated for admin login.
	 * 4. If the credentials are incorrect or null, a failure response is generated
	 * with an appropriate message. 5. Returns a response entity containing a
	 * success flag, a message, and an admin status flag.
	 *
	 * @param login The login object containing email, password, and admin status.
	 * @return A ResponseEntity containing a map with success status, message, and
	 *         admin status flag.
	 */
	public ResponseEntity<Map<String, Object>> login(Login login) {
		Map<String, Object> response = new HashMap<>();
		String email = login.getEmail();
		String password = login.getPassword();
		if (email != null && password != null) {
			LOGGER.info(" authentication for email: {}", email);

			if (email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD)) {
				LOGGER.info("Authentication successful for admin with email: {}", email);
				response.put("success", true);
				response.put("message", "Authentication successful. Welcome, admin.");

				response.put("IS_ADMIN", true);

				LOGGER.info(" authentication for IS_ADMIN: {}", IS_ADMIN);
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				LOGGER.warn("Invalid credentials provided for email: {}", email);
				response.put("success", false);
				response.put("message", "Invalid email or password.");
				response.put("IS_ADMIN", false);
			}
		} else {
			LOGGER.warn("Email or password is null during authentication attempt.");
		}
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}

}
