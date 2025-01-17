package com.mss.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCountByCategoryDTO {
	private String  month;
	private int categoryId;
	private long count;

}
