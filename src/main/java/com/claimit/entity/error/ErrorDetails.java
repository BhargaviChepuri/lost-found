
package com.claimit.entity.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *
 * Class representing the error description details.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {

	private Date timestamp;
	private String status;
	private String message;
	private String details;


}