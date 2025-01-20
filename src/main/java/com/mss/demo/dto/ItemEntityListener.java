
package com.mss.demo.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mss.demo.entity.Items;
import com.mss.demo.service.NotificationService;

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
