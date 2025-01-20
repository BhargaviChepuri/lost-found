<<<<<<< HEAD
package com.mss.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimHistoryDTO {
	private ItemStatus status;
	private int claimId;
	private LocalDateTime claimDate;
	private byte[] image;
	private int requestId;
	private int userId;
	private String userName;
	private String email;

	public ClaimHistoryDTO(int claimId, LocalDateTime claimDate, ItemStatus status, byte[] image, int requestId,
			int userId, String userName, String email) {
        this.claimId = claimId;
        this.claimDate = claimDate;
        this.status = status;
        this.image = image;
        this.requestId = requestId;
        this.userId = userId;
        this.userName = userName;
        this.email = email;
    }



}
=======
package com.mss.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimHistoryDTO {
	private ItemStatus status;
	private int claimId;
	private LocalDateTime claimDate;
	private byte[] image;
	private int requestId;
	private int userId;
	private String userName;
	private String email;

	public ClaimHistoryDTO(int claimId, LocalDateTime claimDate, ItemStatus status, byte[] image, int requestId,
			int userId, String userName, String email) {
        this.claimId = claimId;
        this.claimDate = claimDate;
        this.status = status;
        this.image = image;
        this.requestId = requestId;
        this.userId = userId;
        this.userName = userName;
        this.email = email;
    }



}
>>>>>>> daeb51f8996ebe7c160bda57c1a4cbcd9cafa8e9
