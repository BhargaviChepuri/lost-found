package com.mss.demo.entity;

import java.sql.Date;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int claimId;

	private LocalDateTime claimDate;
	private LocalDateTime verificationDate;
	private String claimNotes;
	private Status claimStatus;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "request_id", nullable = false)
	private ItemsRequest request;

}
