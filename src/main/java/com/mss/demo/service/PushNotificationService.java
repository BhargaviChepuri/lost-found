//package com.mss.demo.service;
//
//import org.springframework.stereotype.Service;
//
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.Message;
//import com.google.firebase.messaging.Notification;
//
//@Service
//public class PushNotificationService {
//
//	public void sendPushNotification(String deviceToken, String title, String body) {
//		try {
//			// Create notification
//			Notification notification = Notification.builder().setTitle(title).setBody(body).build();
//
//			// Build message with device token and notification details
//			Message message = Message.builder().setNotification(notification).setToken(deviceToken).build();
//
//			// Send the push notification
//			String response = FirebaseMessaging.getInstance().sendAsync(message).get();
//
//			// Log the response
//			System.out.println("Push notification sent successfully: " + response);
//
//		} catch (Exception e) {
//			// Handle any exceptions and log
//			System.err.println("Error sending push notification: " + e.getMessage());
//		}
//	}
//}
