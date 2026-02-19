package com.example.note;

import com.example.base.BaseEntity;
import com.example.folder.Folder;
import com.example.tag.Tag;
import com.example.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Note extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant deletedAt;
    private Instant cascadeDeletedAt;

    private String content;


    @ManyToOne(optional = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Folder folder;


    @ManyToMany
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    protected Note() {}

    public Note(String content, User owner, Folder folder) {
        this.content = content;
        this.owner = owner;
        this.folder = folder;
    }

    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PRIVATE;


    public Note(String content, User owner) {
        this.content = content;
        this.owner = owner;
    }


    public User getOwner() {
        return owner;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public boolean isOwnedBy(User user) {
        return this.owner.equals(user);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Instant isCascadeDeleted() {
        return cascadeDeletedAt;
    }

    public void setCascadeDeletedAt(Instant cascadeDeletedAt) {
        this.cascadeDeletedAt = cascadeDeletedAt;
    }
}
