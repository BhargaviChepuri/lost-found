package com.claimit.dto;

import java.util.Date;

public class ItemDTO {
	
	private int itemId;
	private String itemName;
	private ItemStatus status;
	private Date receivedDate;
	private Integer userId;
	private byte[] image;
	private String userName;
	private String email;
	private String categoryName;
	private String description;
	private String uniqueId;

	public ItemDTO(int itemId, String itemName, ItemStatus status, Date receivedDate, Integer userId, byte[] image,
			String userName, String email, String categoryName, String description, String uniqueId) {
		super();
		this.itemId = itemId;
		this.itemName = itemName;
		this.status = status;
		this.receivedDate = receivedDate;
		this.userId = userId;
		this.image = image;
		this.userName = userName;
		this.email = email;
		this.categoryName = categoryName;
		this.description = description;
		this.uniqueId = uniqueId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public ItemStatus getStatus() {
		return status;
	}

	public void setStatus(ItemStatus status) {
		this.status = status;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
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

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

}