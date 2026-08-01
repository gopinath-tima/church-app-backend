package com.church.churchapp.repository;

import com.church.churchapp.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Automatically fetches events and sorts them by date!
    List<Event> findAllByOrderByStartDateAsc();
}