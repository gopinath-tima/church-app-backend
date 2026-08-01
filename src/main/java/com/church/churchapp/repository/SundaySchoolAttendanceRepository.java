package com.church.churchapp.repository;

import com.church.churchapp.entity.SundaySchoolAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SundaySchoolAttendanceRepository extends JpaRepository<SundaySchoolAttendance, Long> {
}
