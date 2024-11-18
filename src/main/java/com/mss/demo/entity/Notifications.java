//package com.mss.demo.entity;
//
//import java.sql.Date;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class Notifications {
//
//
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private int notificationId;
//	private String message;
//	private Date notificationDate;
//
//	@Enumerated(EnumType.STRING)
//	private NotificationStatus status;
//
//	@ManyToOne
//	@JoinColumn(name = "user_id", nullable = false)
//	private User user;
//
//	@ManyToOne
//	@JoinColumn(name = "request_id", nullable = false)
//	private ItemsRequest request;
//
//}
