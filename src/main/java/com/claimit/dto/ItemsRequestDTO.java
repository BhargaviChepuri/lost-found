package com.claimit.dto;

import java.time.LocalDateTime;

public class ItemsRequestDTO {
	
	private int requestId;
	private LocalDateTime claimDate;
	private ItemStatus status;
	private byte[] image;
	private String userName;
	private String email;

	public ItemsRequestDTO(int requestId, LocalDateTime claimDate, ItemStatus status, byte[] image, String userName,
			String email) {
		super();
		this.requestId = requestId;
		this.claimDate = claimDate;
		this.status = status;
		this.image = image;
		this.userName = userName;
		this.email = email;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public LocalDateTime getClaimDate() {
		return claimDate;
	}

	public void setClaimDate(LocalDateTime claimDate) {
		this.claimDate = claimDate;
	}

	public ItemStatus getStatus() {
		return status;
	}

	public void setStatus(ItemStatus status) {
		this.status = status;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
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