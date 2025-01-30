package com.mss.demo.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private int userId;
	private String name;
	@Column(unique = true)
	private String email;
	private String message;
	private boolean isRead;

	@ManyToOne
	@JoinColumn(name = "org_id")
	@JsonBackReference
	private Organisation organisation;
	
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "user")
    @JsonIgnore
    private List<Items> items = new ArrayList<>();
}