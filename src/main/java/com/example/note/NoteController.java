package com.example.note;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteController {
// Dumb controller
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public NoteResponse create(@RequestBody String content, Authentication auth) {
        return noteService.create(content, auth);
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id, Authentication auth) {
        return noteService.getById(id, auth);
    }
}
