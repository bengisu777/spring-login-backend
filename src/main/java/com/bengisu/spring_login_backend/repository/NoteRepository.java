package com.bengisu.spring_login_backend.repository;

import com.bengisu.spring_login_backend.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserId(Long userId);

    Optional<Note> findByIdAndUserId(Long id, Long userId);

}
