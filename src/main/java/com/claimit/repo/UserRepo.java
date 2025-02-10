package com.claimit.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.claimit.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {


	Optional<User> findByEmail(String email);

	List<User> findByItemsItemId(int itemId);

	@Query("SELECT u.userId FROM User u WHERE u.email = :email")
	Integer findUserIdByEmail(@Param("email") String email);

	
	@Query("SELECT u FROM User u WHERE u.email = :email OR u.userName = :userName")
	Optional<User> findByEmailOrUserName(@Param("email") String email, @Param("userName") String userName);
}
