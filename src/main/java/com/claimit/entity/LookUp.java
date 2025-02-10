package com.claimit.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class LookUp {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String name;
	private String type; // "CATEGORY" or "SUBCATEGORY"
	private String status;

    @JsonManagedReference  
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LookUp> children = new ArrayList<>();

    @JsonBackReference 
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private LookUp parent;

	public LookUp() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LookUp getParent() {
		return parent;
	}

	public void setParent(LookUp parent) {
		this.parent = parent;
	}

	public List<LookUp> getChildren() {
		return children;
	}

	public void setChildren(List<LookUp> children) {
		this.children = children;
	}

	public LookUp(int id, String name, String type, String status, LookUp parent, List<LookUp> children) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.status = status;
		this.parent = parent;
		this.children = children;
	}

}
