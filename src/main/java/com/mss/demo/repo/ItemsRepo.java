package com.mss.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mss.demo.entity.Items;

public interface ItemsRepo extends JpaRepository<Items, Integer> {

}
