package com.church.churchapp.repository;

import com.church.churchapp.entity.SundaySchoolProgressReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SundaySchoolProgressReportRepository extends JpaRepository<SundaySchoolProgressReport, Long> {
}
