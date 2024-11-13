package com.mss.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mss.demo.entity.Notifications;

public interface NotificationsRepo extends JpaRepository<Notifications, Integer> {

}
