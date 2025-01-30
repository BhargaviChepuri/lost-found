
package com.mss.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemStatusCountDTO {
	private String month;
	private long totalItems;
	private long unclaimed;
	private long pendingApproval;
	private long pendingPickup;
	private long claimed;
	private long rejected;
	private long archived;

}
