package com.church.churchapp.repository;

import com.church.churchapp.entity.Member;
import com.church.churchapp.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Make sure to import this!

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // ✅ This single line fixes your red error!
    List<Note> findByMember(Member member);

}