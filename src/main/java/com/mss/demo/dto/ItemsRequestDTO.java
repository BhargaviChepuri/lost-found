<<<<<<< HEAD
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

=======
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

>>>>>>> daeb51f8996ebe7c160bda57c1a4cbcd9cafa8e9
}