
package com.mss.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemsRequestDTO {
	private int requestId;
	private LocalDateTime claimDate;
	private ItemStatus status;
	private byte[] image;
	private String userName;
	private String email;

}