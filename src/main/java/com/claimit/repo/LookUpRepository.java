package com.claimit.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.claimit.entity.LookUp;

public interface LookUpRepository extends JpaRepository<LookUp, Integer> {

	List<LookUp> findByType(String type);

	Optional<LookUp> findFirstByName(String name);

}
