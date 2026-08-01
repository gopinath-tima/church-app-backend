package com.church.churchapp.controller;

import com.church.churchapp.entity.*;
import com.church.churchapp.service.SundaySchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sunday-school")
@PreAuthorize("hasAnyRole('MINISTRY', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class SundaySchoolController {

    @Autowired
    private SundaySchoolService sundaySchoolService;

    // --- Teachers Endpoints ---
    @GetMapping("/teachers")
    public ResponseEntity<List<SundaySchoolTeacher>> getAllTeachers() {
        return ResponseEntity.ok(sundaySchoolService.getAllTeachers());
    }

    @PostMapping("/teachers")
    public ResponseEntity<SundaySchoolTeacher> saveTeacher(@RequestBody SundaySchoolTeacher teacher) {
        return ResponseEntity.ok(sundaySchoolService.saveTeacher(teacher));
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        sundaySchoolService.deleteTeacher(id);
        return ResponseEntity.ok().build();
    }

    // --- Classes Endpoints ---
    @GetMapping("/classes")
    public ResponseEntity<List<SundaySchoolClass>> getAllClasses() {
        return ResponseEntity.ok(sundaySchoolService.getAllClasses());
    }

    @PostMapping("/classes")
    public ResponseEntity<SundaySchoolClass> saveClass(@RequestBody SundaySchoolClass schoolClass) {
        return ResponseEntity.ok(sundaySchoolService.saveClass(schoolClass));
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        sundaySchoolService.deleteClass(id);
        return ResponseEntity.ok().build();
    }

    // --- Students Endpoints ---
    @GetMapping("/students")
    public ResponseEntity<List<SundaySchoolStudent>> getAllStudents() {
        return ResponseEntity.ok(sundaySchoolService.getAllStudents());
    }

    @PostMapping("/students")
    public ResponseEntity<SundaySchoolStudent> saveStudent(@RequestBody SundaySchoolStudent student) {
        return ResponseEntity.ok(sundaySchoolService.saveStudent(student));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        sundaySchoolService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }

    // --- Attendance Endpoints ---
    @GetMapping("/attendance")
    public ResponseEntity<List<SundaySchoolAttendance>> getAllAttendance() {
        return ResponseEntity.ok(sundaySchoolService.getAllAttendance());
    }

    @PostMapping("/attendance")
    public ResponseEntity<SundaySchoolAttendance> saveAttendance(@RequestBody SundaySchoolAttendance attendance) {
        return ResponseEntity.ok(sundaySchoolService.saveAttendance(attendance));
    }

    @DeleteMapping("/attendance/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        sundaySchoolService.deleteAttendance(id);
        return ResponseEntity.ok().build();
    }

    // --- Progress Reports Endpoints ---
    @GetMapping("/progress")
    public ResponseEntity<List<SundaySchoolProgressReport>> getAllProgressReports() {
        return ResponseEntity.ok(sundaySchoolService.getAllProgressReports());
    }

    @PostMapping("/progress")
    public ResponseEntity<SundaySchoolProgressReport> saveProgressReport(@RequestBody SundaySchoolProgressReport report) {
        return ResponseEntity.ok(sundaySchoolService.saveProgressReport(report));
    }

    @DeleteMapping("/progress/{id}")
    public ResponseEntity<Void> deleteProgressReport(@PathVariable Long id) {
        sundaySchoolService.deleteProgressReport(id);
        return ResponseEntity.ok().build();
    }
}
