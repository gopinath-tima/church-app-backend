package com.church.churchapp.repository;

import com.church.churchapp.entity.ServingOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServingOpportunityRepository extends JpaRepository<ServingOpportunity, Long> {
    List<ServingOpportunity> findByStatusOrderByDateAsc(String status);
}
