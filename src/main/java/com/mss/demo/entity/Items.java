package com.mss.demo.entity;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Items {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int itemId;

	private String itemName;
	private String status;
	@Temporal(TemporalType.TIMESTAMP)
	private Date foundDate; // createdDate

	private LocalDateTime receivedDate; // receivedDate

	@Lob
	private byte[] image;

	private String dominantColor;

	@Lob
	private String detectedText;

	@PrePersist
	protected void onCreate() {
		this.foundDate = new Date(); // Default to current date
		this.receivedDate = LocalDateTime.now();
		 }

}
