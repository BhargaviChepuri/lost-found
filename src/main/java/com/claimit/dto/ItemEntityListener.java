
package com.claimit.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.claimit.entity.Items;
import com.claimit.service.NotificationService;

import jakarta.persistence.PostUpdate;

@Component
public class ItemEntityListener {

    @Autowired
    private NotificationService notificationService;

    @PostUpdate
    public void onItemUpdate(Items item) {
        notificationService.notifyAndSaveItem();
    }
}
