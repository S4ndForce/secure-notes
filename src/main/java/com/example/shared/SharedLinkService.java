package com.example.shared;

import com.example.exceptions.ForbiddenException;
import com.example.exceptions.NotFoundException;
import com.example.note.Note;
import com.example.note.NoteRepository;
import com.example.note.NoteResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class SharedLinkService {

    private final SharedLinkRepository sharedLinkRepository;
    private final NoteRepository noteRepository;
    public SharedLinkService(SharedLinkRepository repository, NoteRepository noteRepository) {
        this.sharedLinkRepository = repository;
        this.noteRepository = noteRepository;
    }

    public SharedLink create(Note note, Set<SharedAction> actions, Instant expiresAt) {
        String token = UUID.randomUUID().toString();
        SharedLink link = new SharedLink(token, note, actions, expiresAt);
        return sharedLinkRepository.save(link);
    }

    public SharedLink validate(String token, SharedAction action) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid link"));

        if (link.getRevokedAt() != null) {
            throw new ForbiddenException("Link revoked");
        }

        Instant expiresAt = link.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            throw new ForbiddenException("Link expired");
        }

        if (!link.getActions().contains(action)) {
            throw new ForbiddenException("Action not allowed");
        }

        if (link.getNote().getDeletedAt() != null) {
            throw new ForbiddenException("Note deleted");
        }

        return link;
    }

    public void revoke(String token) {
        SharedLink link = sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid link"));

        if (link.getRevokedAt() != null) {
            return;
        }

        link.revoke(Instant.now());
        sharedLinkRepository.save(link);
    }

    public NoteResponse getNote(String token) {
        SharedLink link = validate(token, SharedAction.READ);
        return NoteResponse.fromEntity(link.getNote());
    }

    @Transactional
    public NoteResponse updateViaSharedLink(String token, SharedLinkUpdateRequest request) {
        SharedLink link = validate(token, SharedAction.UPDATE);
        Note note = link.getNote();


        if (request.content() != null) {
            note.setContent(request.content());
            note.setUpdatedAt(Instant.now());
        }
        note = noteRepository.save(note);

        return NoteResponse.fromEntity(note);
    }
}
