package com.claimit.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LookUp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public int id;
	public String code;
	public String name;
	public boolean status;

}
