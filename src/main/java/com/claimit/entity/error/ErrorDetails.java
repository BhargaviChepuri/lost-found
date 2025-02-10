package com.claimit.entity.error;

import java.util.Date;

/**
 *
 * Class representing the error description details.
 */

public class ErrorDetails {

	private Date timestamp;
	private String status;
	private String message;
	private String details;

	public ErrorDetails(Date timestamp, String status, String message, String details) {
		super();
		this.timestamp = timestamp;
		this.status = status;
		this.message = message;
		this.details = details;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

}