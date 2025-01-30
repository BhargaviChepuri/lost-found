
package com.mss.demo.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mss.demo.dto.ItemStatus;

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

}
