package com.mss.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimHistory {

	@Id
	private int id;

	private String receiverName;

	private String phoneNumber;

	private String status;

	private LocalDateTime receivedDate;

	@ManyToOne
	@JoinColumn(name = "item_id", referencedColumnName = "id")
	private Items item;

	public LocalDateTime getReceivedDate() {
		return this.item.getReceivedDate();
	}

	@PrePersist
	public void setReceivedDateFromItem() {
		if (this.item != null) {
			this.receivedDate = this.item.getReceivedDate(); // Copy receivedDate from associated item
		}
	}

}
