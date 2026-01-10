package com.example.note;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;


import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {
// Dumb controller
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public NoteResponse create(
            @RequestParam Long folderId,
            @RequestBody String content,
            Authentication auth
    ) {
        return noteService.create(folderId, content, auth);
    }

    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable Long id, Authentication auth) {

        return noteService.getById(id, auth);
    }

    @GetMapping
    public List<NoteResponse> getMyNotes(Authentication auth) {
        return noteService.getMyNotes(auth);
    }

    @PatchMapping("/{id}")
    public NoteResponse update(
            @PathVariable Long id,
            @RequestBody String content,
            Authentication auth
    ) {
        System.out.println("PATCH HIT");
        return noteService.update(id, content, auth);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            Authentication auth
    ) {
         noteService.delete(id, auth);
    }

    @GetMapping("/folder/{folderId}")
    public List<NoteResponse> getByFolder(
            @PathVariable Long folderId,
            Authentication auth
    ) {
        return noteService.getByFolder(folderId, auth);
    }

    @PostMapping("/{id}/share")
    public String share(@PathVariable Long id, Authentication auth) {
        return noteService.createSharedLink(id, auth);
    }

    @GetMapping("/search")
    public List<NoteResponse> searchMyNotes(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Long folderId,
            Authentication auth
    ) {
        return noteService.searchMyNotes(text, folderId, auth);
    }
}
