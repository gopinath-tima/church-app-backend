package com.church.churchapp.service;

import com.church.churchapp.entity.*;
import com.church.churchapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SundaySchoolService {

    @Autowired
    private SundaySchoolTeacherRepository teacherRepository;

    @Autowired
    private SundaySchoolClassRepository classRepository;

    @Autowired
    private SundaySchoolStudentRepository studentRepository;

    @Autowired
    private SundaySchoolAttendanceRepository attendanceRepository;

    @Autowired
    private SundaySchoolProgressReportRepository progressReportRepository;

    @Autowired
    private EventRepository eventRepository;

    // --- Teachers CRUD ---
    public List<SundaySchoolTeacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public SundaySchoolTeacher saveTeacher(SundaySchoolTeacher teacher) {
        SundaySchoolTeacher saved = teacherRepository.save(teacher);
        // Find classes taught by this teacher and update their event organizer info
        List<SundaySchoolClass> classes = classRepository.findAll();
        for (SundaySchoolClass c : classes) {
            if (saved.getId().equals(c.getTeacherId())) {
                updateClassEvent(c, saved.getName());
            }
        }
        return saved;
    }

    public void deleteTeacher(Long id) {
        teacherRepository.deleteById(id);
    }

    // --- Classes CRUD ---
    public List<SundaySchoolClass> getAllClasses() {
        return classRepository.findAll();
    }

    public SundaySchoolClass saveClass(SundaySchoolClass schoolClass) {
        String teacherName = "Unassigned";
        if (schoolClass.getTeacherId() != null) {
            teacherName = teacherRepository.findById(schoolClass.getTeacherId())
                .map(SundaySchoolTeacher::getName)
                .orElse("Unassigned");
        }
        
        updateClassEvent(schoolClass, teacherName);
        return classRepository.save(schoolClass);
    }

    private void updateClassEvent(SundaySchoolClass schoolClass, String teacherName) {
        Event event = null;
        if (schoolClass.getEventId() != null) {
            event = eventRepository.findById(schoolClass.getEventId()).orElse(null);
        }
        if (event == null) {
            event = new Event();
        }
        event.setTitle(schoolClass.getName() + " (Sunday School)");
        event.setDescription((schoolClass.getDescription() != null ? schoolClass.getDescription() : "") + "\nTeacher: " + teacherName);
        event.setLocation(schoolClass.getRoom());
        event.setOrganizer(teacherName);
        event.setTargetAudience("Class");
        
        String[] dates = calculateEventDates(schoolClass.getName(), schoolClass.getTime());
        event.setStartDate(dates[0]);
        event.setEndDate(dates[1]);
        
        Event savedEvent = eventRepository.save(event);
        schoolClass.setEventId(savedEvent.getEventId());
    }

    private String[] calculateEventDates(String className, String classTime) {
        java.time.LocalDate date = java.time.LocalDate.now();
        java.time.DayOfWeek day = java.time.DayOfWeek.SUNDAY;
        
        String lowerName = (className != null ? className : "").toLowerCase();
        if (lowerName.contains("monday")) day = java.time.DayOfWeek.MONDAY;
        else if (lowerName.contains("tuesday")) day = java.time.DayOfWeek.TUESDAY;
        else if (lowerName.contains("wednesday")) day = java.time.DayOfWeek.WEDNESDAY;
        else if (lowerName.contains("thursday")) day = java.time.DayOfWeek.THURSDAY;
        else if (lowerName.contains("friday")) day = java.time.DayOfWeek.FRIDAY;
        else if (lowerName.contains("saturday")) day = java.time.DayOfWeek.SATURDAY;
        
        date = date.with(java.time.temporal.TemporalAdjusters.nextOrSame(day));
        
        int hour = 10;
        int minute = 0;
        try {
            String t = (classTime != null ? classTime : "").toLowerCase().replaceAll("\\s+", "");
            boolean pm = t.contains("pm");
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)(?::(\\d+))?").matcher(t);
            if (m.find()) {
                hour = Integer.parseInt(m.group(1));
                if (m.group(2) != null) {
                    minute = Integer.parseInt(m.group(2));
                }
                if (pm && hour < 12) hour += 12;
                if (!pm && hour == 12) hour = 0;
            }
        } catch (Exception e) {
            // default fallback
        }
        
        java.time.LocalDateTime start = date.atTime(hour, minute);
        java.time.LocalDateTime end = start.plusHours(1);
        
        return new String[] {
            start.toString().substring(0, 16),
            end.toString().substring(0, 16)
        };
    }

    public void deleteClass(Long id) {
        classRepository.findById(id).ifPresent(c -> {
            if (c.getEventId() != null) {
                try {
                    eventRepository.deleteById(c.getEventId());
                } catch (Exception e) {
                    // Ignore if already deleted
                }
            }
            classRepository.delete(c);
        });
    }

    // --- Students CRUD ---
    public List<SundaySchoolStudent> getAllStudents() {
        return studentRepository.findAll();
    }

    public SundaySchoolStudent saveStudent(SundaySchoolStudent student) {
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    // --- Attendance CRUD ---
    public List<SundaySchoolAttendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public SundaySchoolAttendance saveAttendance(SundaySchoolAttendance attendance) {
        return attendanceRepository.save(attendance);
    }

    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

    // --- Progress Reports CRUD ---
    public List<SundaySchoolProgressReport> getAllProgressReports() {
        return progressReportRepository.findAll();
    }

    public SundaySchoolProgressReport saveProgressReport(SundaySchoolProgressReport report) {
        return progressReportRepository.save(report);
    }

    public void deleteProgressReport(Long id) {
        progressReportRepository.deleteById(id);
    }
}
