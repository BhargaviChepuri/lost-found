package com.claimit.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.claimit.entity.LookUp;
import com.claimit.entity.Organisation;

public interface LookUpRepository extends JpaRepository<LookUp, Integer> {

	@Query("SELECT s FROM LookUp s WHERE code='SC' AND LOWER(s.name) LIKE %:name%")
	List<LookUp> findByNameLikeIgnoreCase(@Param("name") String name);
	
	@Query("SELECT s FROM LookUp s WHERE code='C' AND LOWER(s.name) LIKE %:name%")
	List<LookUp> findByNameLikeCase(@Param("name") String name);
	
	List<LookUp> findByNameLikeIgnoreCaseAndCode(String name, String code);



	
	
}
