package com.example.note;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;


import java.util.List;
import java.util.Set;

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
    public List<NoteResponse> getMyFilteredNotes(Authentication auth) {
        return noteService.getMyFilteredNotes(auth);
    }

    @PatchMapping("/{id}")
    public NoteResponse update(
            @PathVariable Long id,
            @RequestBody String content,
            Authentication auth
    ) {
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
    public String share(@PathVariable Long id, @RequestParam Long expiration, Authentication auth) {
        return noteService.createSharedLink(id, expiration, auth);
    }

    @GetMapping("/search")
    public PageResponse<NoteResponse> search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String tagName,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication auth
    ) {
        return noteService.searchMyNotes(text, folderId, pageable, tagName, auth);
    }

    @PostMapping("/{id}/tags")
    public NoteResponse addTags(
            @PathVariable Long id,
            @RequestBody Set<String> tags,
            Authentication auth
    ) {
        return noteService.addTags(id, tags, auth);
    }

    @DeleteMapping("/{id}/tags/{name}")
    public NoteResponse removeTag(
            @PathVariable Long id,
            @PathVariable String name,
            Authentication auth
    ) {
        return noteService.removeTag(id, name, auth);
    }

    @PostMapping("/{id}/restore")
    public void restore(@PathVariable Long id, Authentication auth) {
        noteService.restore(id, auth);
    }
}
