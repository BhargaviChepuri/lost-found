package com.mss.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mss.demo.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {

	//List<User> findByEmail(String userEmail);

//	 List<User> findByEmail(String emailId);

	 Page<User> findByNameContainingIgnoreCase(String searchTerm, Pageable pageable);

	Optional<User> findByEmail(String email);


}
