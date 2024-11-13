package com.mss.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notifications {

	@Id
	private int id;

	@Column(name = "sender")
	private String from;

	@Column(name = "receiver")
	private String to;

	private String message;

}
