package com.church.churchapp.repository;

import com.church.churchapp.entity.Ministry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MinistryRepository extends JpaRepository<Ministry, Long> {

    Optional<Ministry> findByMinistryName(String ministryName);
}