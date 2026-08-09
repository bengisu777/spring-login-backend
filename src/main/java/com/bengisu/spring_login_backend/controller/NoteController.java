package com.bengisu.spring_login_backend.controller;

import com.bengisu.spring_login_backend.model.Note;
import com.bengisu.spring_login_backend.repository.NoteRepository;
import com.bengisu.spring_login_backend.repository.UserRepository;
import com.bengisu.spring_login_backend.model.User;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteController(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    record NoteRequest(String title, String content) {
    }

    record NoteResponse(Long id, String title, String content, LocalDateTime createdAt) {
    }

    record ErrorResponse(String error) {
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow();
    }

    private NoteResponse toResponse(Note note) {
        return new NoteResponse(note.getId(), note.getTitle(), note.getContent(), note.getCreatedAt());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody NoteRequest request, Authentication authentication) {

        if (request.title() == null || request.title().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Başlık zorunlu"));
        }

        Note note = new Note();
        note.setTitle(request.title());
        note.setContent(request.content());
        note.setUser(currentUser(authentication));

        Note saved = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public List<NoteResponse> list(Authentication authentication) {
        return noteRepository.findByUserId(currentUser(authentication).getId()).stream().map(this::toResponse).toList();
    }

}
