
package com.mss.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mss.demo.entity.User;

public interface UserRepo extends JpaRepository<User, Integer> {


	Optional<User> findByEmail(String email);

	List<User> findByItems_ItemId(int itemId);


	@Query("SELECT u.userId FROM User u WHERE u.email = :email")
	Integer findUserIdByEmail(@Param("email") String email);

//	Optional<User> findByEmailAndName(String email, String userName);
	
	@Query("SELECT u FROM User u WHERE u.email = :email OR u.name = :userName")
	Optional<User> findByEmailOrUserName(@Param("email") String email, @Param("userName") String userName);
}
