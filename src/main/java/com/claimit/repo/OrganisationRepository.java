
package com.claimit.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.claimit.entity.Organisation;

public interface OrganisationRepository extends JpaRepository<Organisation, String> {

	Optional<Organisation> findByOrgId(String orgId);

}

