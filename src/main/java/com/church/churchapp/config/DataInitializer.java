package com.church.churchapp.config;

import com.church.churchapp.entity.User;
import com.church.churchapp.enums.Role;
import com.church.churchapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set; // ✅ Required to create the List of roles

@Configuration
public class DataInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.church.churchapp.repository.EventRepository eventRepository;

    @Autowired
    private com.church.churchapp.repository.SundaySchoolClassRepository classRepository;

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 🔄 Synchronize existing events where targetAudience = 'Class' with SundaySchoolClass
            try {
                java.util.List<com.church.churchapp.entity.Event> classEvents = eventRepository.findAll();
                for (com.church.churchapp.entity.Event ev : classEvents) {
                    if (ev.getTargetAudience() != null && ev.getTargetAudience().equalsIgnoreCase("Class")) {
                        if (classRepository.findByEventId(ev.getEventId()).isEmpty()) {
                            com.church.churchapp.entity.SundaySchoolClass ssClass = new com.church.churchapp.entity.SundaySchoolClass();
                            ssClass.setEventId(ev.getEventId());
                            ssClass.setName(ev.getTitle());
                            ssClass.setRoom(ev.getLocation());
                            ssClass.setDescription(ev.getDescription());
                            ssClass.setTeacherId(ev.getTeacherId());
                            
                            // Extract time from startDate
                            if (ev.getStartDate() != null && ev.getStartDate().contains("T")) {
                                try {
                                    String timePart = ev.getStartDate().split("T")[1];
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
                            System.out.println("✅ Synced class event to Sunday School: " + ev.getTitle());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to sync class events: " + e.getMessage());
            }
            // 🔄 Auto-migrate PostgreSQL OID columns to bytea at startup
            try {
                jdbcTemplate.execute("ALTER TABLE documents ALTER COLUMN file_data TYPE bytea USING lo_get(file_data)");
                System.out.println("✅ Documents table file_data column migrated to bytea successfully!");
            } catch (Exception e) {
                // Thrown if already converted or table not yet created
                System.out.println("ℹ️ Documents table migration skipped/already completed: " + e.getMessage());
            }

            try {
                jdbcTemplate.execute("ALTER TABLE members ALTER COLUMN profile_photo TYPE bytea USING lo_get(profile_photo)");
                System.out.println("✅ Members table profile_photo column migrated to bytea successfully!");
            } catch (Exception e) {
                // Thrown if already converted or table not yet created
                System.out.println("ℹ️ Members table migration skipped/already completed: " + e.getMessage());
            }

            // Check if the master admin already exists
            if (userRepository.findByUsername("gobi").isEmpty()) {

                User superAdmin = new User();
                superAdmin.setUsername("gobi");
                // You can change "password" to whatever default you were using
                superAdmin.setPassword(passwordEncoder.encode("password"));

                // ✅ Updated: Now uses setRoles() and wraps the role in Set.of()
                superAdmin.setRoles(Set.of(Role.SUPER_PLUS_ADMIN));

                userRepository.save(superAdmin);
                System.out.println("Default Super Plus Admin 'gobi' created successfully!");
            }
        };
    }
}