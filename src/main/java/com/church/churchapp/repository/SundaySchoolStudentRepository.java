package com.church.churchapp.repository;

import com.church.churchapp.entity.SundaySchoolStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SundaySchoolStudentRepository extends JpaRepository<SundaySchoolStudent, Long> {
}
