package com.mss.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchItemDto {
	private String title;
	private String itemName;
	private String description;
	private byte[] image;

}
