package com.example.shared;

import com.example.exceptions.ForbiddenException;
import com.example.exceptions.NotFoundException;
import com.example.note.Note;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class SharedLinkService {

    private final SharedLinkRepository repository;

    public SharedLinkService(SharedLinkRepository repository) {
        this.repository = repository;
    }

    public SharedLink create(Note note, Set<SharedAction> actions, Instant expiresAt) {
        String token = UUID.randomUUID().toString();
        SharedLink link = new SharedLink(token, note, actions, expiresAt);
        return repository.save(link);
    }

    public SharedLink validate(String token, SharedAction action) {
        SharedLink link = repository.findByToken(token)
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

        return link;
    }

    public void revoke(String token) {
        SharedLink link = repository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invalid link"));

        if (link.getRevokedAt() != null) {
            return;
        }

        link.revoke(Instant.now());
        repository.save(link);
    }
}
