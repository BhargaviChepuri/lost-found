
package com.claimit.entity;

import java.time.LocalDateTime;

import com.claimit.dto.ItemStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ClaimHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int claimId;

	private LocalDateTime claimDate;

	@Enumerated(EnumType.STRING)
	private ItemStatus claimStatus;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@JsonBackReference
	private User user;

	@Column(name = "user_email", nullable = false)
	private String userEmail;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "request_id", nullable = true)
	private ItemsRequest request;

	private int itemId;

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

	public ItemStatus getClaimStatus() {
		return claimStatus;
	}

	public void setClaimStatus(ItemStatus claimStatus) {
		this.claimStatus = claimStatus;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public ItemsRequest getRequest() {
		return request;
	}

	public void setRequest(ItemsRequest request) {
		this.request = request;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

}
