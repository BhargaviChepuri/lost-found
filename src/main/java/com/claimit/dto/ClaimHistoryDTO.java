package com.claimit.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimHistoryDTO {
	
	private ItemStatus status;
	private int claimId;
	private LocalDateTime claimDate;
	private byte[] image;
	private int requestId;
	private int userId;
	private String userName;
	private String email;

	public ClaimHistoryDTO(int claimId, LocalDateTime claimDate, ItemStatus claimStatus, byte[] image, int requestId,
			int userId, String userName, String userEmail) {
		this.claimId = claimId;
		this.claimDate = claimDate;
		this.status = claimStatus;
		this.image = image;
		this.requestId = requestId;
		this.userId = userId;
		this.userName = userName;
		this.email = userEmail;
	}

	public ItemStatus getStatus() {
		return status;
	}

	public void setStatus(ItemStatus status) {
		this.status = status;
	}

	public int getClaimId() {
		return claimId;
	}

	public void setClaimId(int claimId) {
		this.claimId = claimId;
	}

	public LocalDateTime getClaimDate() {
		return claimDate;
	}

	public void setClaimDate(LocalDateTime claimDate) {
		this.claimDate = claimDate;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
