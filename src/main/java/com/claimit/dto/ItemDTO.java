
package com.claimit.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {
	private int itemId;
	private String itemName;
	private ItemStatus status;
	private Date receivedDate;
	private Integer userId;
	private byte[] image;
	private String userName;
	private String email;
	private String categoryName;
	private String description;
	private String uniqueId;
	
}