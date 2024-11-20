package com.mss.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mss.demo.entity.ItemsRequest;

public interface ItemRequestRepo extends JpaRepository<ItemsRequest, Integer> {

}
