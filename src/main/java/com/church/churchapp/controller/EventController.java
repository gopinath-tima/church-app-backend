package com.church.churchapp.controller;

import com.church.churchapp.entity.Event;
import com.church.churchapp.entity.SundaySchoolClass;
import com.church.churchapp.entity.SundaySchoolTeacher;
import com.church.churchapp.repository.EventRepository;
import com.church.churchapp.repository.SundaySchoolClassRepository;
import com.church.churchapp.repository.SundaySchoolTeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
@PreAuthorize("hasAnyRole('EVENTS', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SundaySchoolClassRepository classRepository;

    @Autowired
    private SundaySchoolTeacherRepository teacherRepository;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAllByOrderByStartDateAsc();
    }

    @PostMapping("/add")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        if (event.getTeacherId() != null) {
            String teacherName = teacherRepository.findById(event.getTeacherId())
                .map(SundaySchoolTeacher::getName)
                .orElse("Unassigned");
            event.setOrganizer(teacherName);
        }
        Event savedEvent = eventRepository.save(event);
        syncSundaySchoolClass(savedEvent);
        return ResponseEntity.ok(savedEvent);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        Optional<Event> optionalEvent = eventRepository.findById(id);

        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            event.setTitle(eventDetails.getTitle());
            event.setDescription(eventDetails.getDescription());
            event.setStartDate(eventDetails.getStartDate());
            event.setEndDate(eventDetails.getEndDate());
            event.setLocation(eventDetails.getLocation());
            event.setTargetAudience(eventDetails.getTargetAudience());
            event.setTeacherId(eventDetails.getTeacherId());

            if (eventDetails.getTeacherId() != null) {
                String teacherName = teacherRepository.findById(eventDetails.getTeacherId())
                    .map(SundaySchoolTeacher::getName)
                    .orElse("Unassigned");
                event.setOrganizer(teacherName);
            } else {
                event.setOrganizer(eventDetails.getOrganizer());
            }

            Event savedEvent = eventRepository.save(event);
            syncSundaySchoolClass(savedEvent);
            return ResponseEntity.ok(savedEvent);
        } else {
            return ResponseEntity.badRequest().body("Event not found");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        Optional<Event> optionalEvent = eventRepository.findById(id);

        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();
            if (event.getTargetAudience() != null && event.getTargetAudience().equalsIgnoreCase("Class")) {
                classRepository.findByEventId(id).ifPresent(c -> classRepository.delete(c));
            }
            eventRepository.delete(event);
            return ResponseEntity.ok("Event deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Event not found");
        }
    }

    private void syncSundaySchoolClass(Event savedEvent) {
        if (savedEvent.getTargetAudience() != null && savedEvent.getTargetAudience().equalsIgnoreCase("Class")) {
            SundaySchoolClass ssClass = classRepository.findByEventId(savedEvent.getEventId())
                .orElse(new SundaySchoolClass());
            ssClass.setEventId(savedEvent.getEventId());
            ssClass.setName(savedEvent.getTitle());
            ssClass.setRoom(savedEvent.getLocation());
            ssClass.setDescription(savedEvent.getDescription());
            ssClass.setTeacherId(savedEvent.getTeacherId());
            
            // Extract time from startDate (e.g. "2026-06-29T10:00" -> "10:00 AM")
            if (savedEvent.getStartDate() != null && savedEvent.getStartDate().contains("T")) {
                try {
                    String timePart = savedEvent.getStartDate().split("T")[1];
                    int hr = Integer.parseInt(timePart.split(":")[0]);
                    int min = Integer.parseInt(timePart.split(":")[1]);
                    String ampm = hr >= 12 ? "PM" : "AM";
                    hr = hr % 12;
                    if (hr == 0) hr = 12;
                    ssClass.setTime(String.format("%02d:%02d %s", hr, min, ampm));
                } catch (Exception e) {
                    ssClass.setTime("10:00 AM");
                }
            } else {
                ssClass.setTime("10:00 AM");
            }
            classRepository.save(ssClass);
        }
    }
}