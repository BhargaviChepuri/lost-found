package com.mss.demo.entity;

import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Items {

	@Id
	private int id;

	private String itemName;

	private String itemDescription;

	private Blob itemImageUrl;

	private LocalDate createdDate;

	private LocalDateTime  receivedDate;

	private String status;

	@OneToMany(mappedBy = "item")
	private List<ClaimHistory> claimHistories;

}
