
package com.claimit.dto;

import java.util.Date;

public class ItemsSearchDTO {
	
	private int itemId;
	private Date receivedDate; // createdDate
	private int subcatgeoryId;
	private int categoryId;
	private Date expirationDate;
	private String colour;
	private String detectedText;
	private String orgId;
	private String description;
	private String title;
	private String itemName;
	private ItemStatus status;
	private Integer userId;
	private byte[] image;
	private String userName;
	private String email;

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public int getSubcatgeoryId() {
		return subcatgeoryId;
	}

	public void setSubcatgeoryId(int subcatgeoryId) {
		this.subcatgeoryId = subcatgeoryId;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public String getDetectedText() {
		return detectedText;
	}

	public void setDetectedText(String detectedText) {
		this.detectedText = detectedText;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	private String categoryName;

	public ItemsSearchDTO(int itemId, Date receivedDate, Date expirationDate, String colour, String detectedText,
			String orgId, String description, String title, String itemName, ItemStatus status, Integer userId,
			byte[] image, String userName, String email, String categoryName) {
		this.itemId = itemId;
		this.receivedDate = receivedDate;
		this.expirationDate = expirationDate;
		this.colour = colour;
		this.detectedText = detectedText;
		this.orgId = orgId;
		this.description = description;
		this.title = title;
		this.itemName = itemName;
		this.status = status;
		this.userId = userId;
		this.image = image;
		this.userName = userName;
		this.email = email;
		this.categoryName = categoryName;
	}

}
