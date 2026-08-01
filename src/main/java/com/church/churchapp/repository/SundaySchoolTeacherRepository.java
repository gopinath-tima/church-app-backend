package com.church.churchapp.repository;

import com.church.churchapp.entity.SundaySchoolTeacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SundaySchoolTeacherRepository extends JpaRepository<SundaySchoolTeacher, Long> {
}
