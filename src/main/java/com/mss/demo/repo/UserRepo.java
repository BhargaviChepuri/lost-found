package com.mss.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mss.demo.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {

	User findByEmail(String userEmail);

}
