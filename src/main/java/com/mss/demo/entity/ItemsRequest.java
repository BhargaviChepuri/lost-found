
package com.mss.demo.entity;

import java.time.LocalDateTime;

import com.mss.demo.dto.ItemStatus;

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
public class ItemsRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int requestId;
	
	private LocalDateTime claimedDate;
	
    private String rejectedReason;
	
	@Enumerated(EnumType.STRING)
	private ItemStatus status;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "item_id", nullable = false)
	private Items item;



}
