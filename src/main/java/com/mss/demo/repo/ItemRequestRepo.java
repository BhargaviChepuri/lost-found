package com.mss.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mss.demo.entity.ItemRequest;

public interface ItemRequestRepo extends JpaRepository<ItemRequest, Integer> {

}
