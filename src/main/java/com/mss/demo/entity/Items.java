<<<<<<< HEAD
package com.mss.demo.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mss.demo.dto.ItemStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Items {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int itemId;

	private String itemName;

	@Enumerated(EnumType.STRING)
	private ItemStatus status;

	private Date receivedDate; // createdDate

	private int subcatgeoryId;

	private int categoryId;

	private Date expirationDate;

	@Lob
	private byte[] image;

	private String dominantColor;

	@Lob
	private String detectedText;

	private String orgId;

	@Column(length = 255)
	private String description;
	
	private String title;

	private String carbonWeight;

	private Double latitude;

	private Double longitude;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private User user;
	private String uniqueId;

}
=======
package com.mss.demo.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mss.demo.dto.ItemStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Items {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int itemId;

	private String itemName;

	@Enumerated(EnumType.STRING)
	private ItemStatus status;

	private Date receivedDate; // createdDate

	private int subcatgeoryId;

	private int categoryId;

	private Date expirationDate;

	@Lob
	private byte[] image;

	private String dominantColor;

	@Lob
	private String detectedText;

	private String orgId;

	@Column(length = 255)
	private String description;
	
	private String title;

	private String carbonWeight;

	private Double latitude;

	private Double longitude;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private User user;
	private String uniqueId;

}
>>>>>>> daeb51f8996ebe7c160bda57c1a4cbcd9cafa8e9
