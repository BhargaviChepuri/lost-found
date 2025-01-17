package com.mss.demo.dto;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExpiredItemDTO {
	private int itemId;
	private ItemStatus status;
	private Date foundDate;
	private Date expirationDate;
	private byte[] image;

	public ExpiredItemDTO(int itemId, ItemStatus status, Date foundDate, Date expirationDate, byte[] image) {
		this.itemId = itemId;
		this.status = status;
		this.foundDate = foundDate;
		this.expirationDate = expirationDate;
		this.image = image;
	}

}
