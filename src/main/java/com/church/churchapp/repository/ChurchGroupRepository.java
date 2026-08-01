package com.church.churchapp.repository;

import com.church.churchapp.entity.ChurchGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChurchGroupRepository extends JpaRepository<ChurchGroup, Long> {
    List<ChurchGroup> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
}
