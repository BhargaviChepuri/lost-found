
package com.mss.demo.dto;

import com.mss.demo.entity.ClaimHistory;
import com.mss.demo.service.NotificationService;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;

public class ClaimHistoryListener {

	private boolean isNotificationSent = false;

	@PostPersist
	@PostUpdate
	public void onClaimHistoryChange(ClaimHistory claimHistory) {
		if (!isNotificationSent) {
			isNotificationSent = true;

			int claimId = claimHistory.getClaimId();

			NotificationService notificationService = ApplicationContextProvider.getBean(NotificationService.class);
			notificationService.handleStatusChangeNotification(claimHistory);
		}
	}
}
