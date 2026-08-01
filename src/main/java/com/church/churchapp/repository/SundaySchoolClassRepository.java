package com.church.churchapp.repository;

import com.church.churchapp.entity.SundaySchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SundaySchoolClassRepository extends JpaRepository<SundaySchoolClass, Long> {
    java.util.Optional<SundaySchoolClass> findByEventId(Long eventId);
}
