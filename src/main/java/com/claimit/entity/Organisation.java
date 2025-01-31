package com.claimit.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
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
public class Organisation {

	@Id
	private String orgId;

	@Column(name = "organisationEmail")
	private String orgEmail;

	@Column(name = "organisationName")
	private String orgName;

	@OneToMany(mappedBy = "organisation")
	@JsonManagedReference
	private List<User> users;



}
