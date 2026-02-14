package com.example.shared;

import com.example.exceptions.ForbiddenException;
import com.example.exceptions.NotFoundException;
import com.example.note.Note;
import com.example.note.NoteRepository;
import com.example.note.NoteResponse;
import com.example.note.NoteService;
import com.example.user.User;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class SharedLinkService {

    private final SharedLinkRepository sharedLinkRepository;
    private final NoteRepository noteRepository;
    private static final Logger log = LoggerFactory.getLogger(SharedLinkService.class);
    public SharedLinkService(SharedLinkRepository repository, NoteRepository noteRepository) {
        this.sharedLinkRepository = repository;
        this.noteRepository = noteRepository;
    }

    public SharedLink create(Note note, Set<SharedAction> actions, User creator, Instant expiresAt) {
        String token = UUID.randomUUID().toString();
        SharedLink link = new SharedLink(token, note, creator, actions, expiresAt);
        sharedLinkRepository.save(link);
        log.info("Shared link created: creator={}, id={}, time={}",
                link.getCreator().getEmail(), link.getId(), Instant.now());
        return link;
    }

    public SharedLink validate(String token, SharedAction action) {
        SharedLink link=  sharedLinkRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid link"));

        if (link.getRevokedAt() != null) {
            log.warn("Attempt to use revoked link: creator={}, id={}, time={}",
                    link.getCreator().getEmail(), link.getId(), Instant.now());
            throw new ForbiddenException("Link revoked");
        }

        Instant expiresAt = link.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            log.warn("Attempt to use expired link: creator={}, id={}, time={}",
                    link.getCreator().getEmail(), link.getId(), Instant.now());
            throw new ForbiddenException("Link expired");
        }

        if (!link.getActions().contains(action)) {
            log.warn("Attempt to use forbidden action link: creator={}, id={}, time={}",
                    link.getCreator().getEmail(), link.getId(), Instant.now());
            throw new ForbiddenException("Action not allowed");
        }

        if (link.getNote().getDeletedAt() != null) {
            log.warn("Attempt to access deleted link: creator={}, id={}, time={}",
                    link.getCreator().getEmail(), link.getId(), Instant.now());
            throw new ForbiddenException("Note deleted");
        }
        if (link.getNote().getFolder().getDeletedAt() != null) {
            log.warn("Attempt to access deleted link: creator={}, id={}, time={}",
                    link.getCreator().getEmail(), link.getId(), Instant.now());
            throw new ForbiddenException("Note's folder deleted");
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
        log.info("Shared link accessed: creator={}, id={}, time={}",
                link.getCreator().getEmail(), link.getId(), Instant.now());
        return NoteResponse.fromEntity(link.getNote());
    }

    //TODO: make append only
    @Transactional
    public NoteResponse updateViaSharedLink(String token, SharedLinkUpdateRequest request) {
        SharedLink link = validate(token, SharedAction.UPDATE);
        Note note = link.getNote();

        if (request.content() != null) {
            note.setContent(request.content());
            note.setUpdatedAt(Instant.now());
        }
        noteRepository.save(note);
        log.info("Shared link updated: creator={}, id={}, time={}",
                link.getCreator().getEmail(), link.getId(), Instant.now());
        return NoteResponse.fromEntity(note);
    }

    //TODO: option to view all shared links owned by user
}
