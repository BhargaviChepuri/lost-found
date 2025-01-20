
package com.claimit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimRequestDTO {

	private String userName;
	private String userEmail;
	private int itemId;

}
