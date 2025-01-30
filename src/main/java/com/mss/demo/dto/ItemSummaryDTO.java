
package com.mss.demo.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemSummaryDTO {

	private int itemId;
	private Date receivedDate;
	private ItemStatus status;
	private String description;
	private byte[] image;
	private String uniqueId;
	private String categoryName;
	private String itemName;

}