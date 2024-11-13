package com.mss.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {

	@Id
	private int id;

	private String itemId;

	private LocalDateTime  requestDate;

	private int userId;

	private String status;

}
