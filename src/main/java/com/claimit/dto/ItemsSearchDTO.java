
package com.claimit.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemsSearchDTO {
	private int itemId;
	private Date receivedDate; // createdDate
	private int subcatgeoryId;
	private int categoryId;
	private Date expirationDate;
	private String colour;
	private String detectedText;
	private String orgId;
	private String description;
	private String title;
	private String itemName;
	private ItemStatus status;
	private Integer userId;
	private byte[] image;
	private String userName;
	private String email;
	private String categoryName;


	public ItemsSearchDTO(int itemId, Date receivedDate, Date expirationDate, String colour, String detectedText,
			String orgId, String description, String title, String itemName, ItemStatus status,
			 Integer userId, byte[] image, String userName, String email, String categoryName) {
		this.itemId = itemId;
		this.receivedDate = receivedDate;
		this.expirationDate = expirationDate;
		this.colour = colour;
		this.detectedText = detectedText;
		this.orgId = orgId;
		this.description = description;
		this.title = title;
		this.itemName = itemName;
		this.status = status;
		this.userId = userId;
		this.image = image;
		this.userName = userName;
		this.email = email;
		this.categoryName = categoryName;
	}

}
