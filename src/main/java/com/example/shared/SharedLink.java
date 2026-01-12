package com.example.shared;

import com.example.note.Note;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Set;

@Entity
public class SharedLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne(optional = false)
    private Note note;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shared_link_actions", joinColumns = @JoinColumn(name = "shared_link_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private Set<SharedAction> actions;

    private Instant expiresAt;

    private Instant revokedAt;

    protected SharedLink() {
    }

    public SharedLink(String token, Note note, Set<SharedAction> actions, Instant expiresAt) {
        this.token = token;
        this.note = note;
        this.actions = actions;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Note getNote() {
        return note;
    }

    public Set<SharedAction> getActions() {
        return actions;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void revoke(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
