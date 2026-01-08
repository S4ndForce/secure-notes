package com.example.note;

import com.example.folder.Folder;
import com.example.user.User;
import jakarta.persistence.*;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(optional = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Folder folder;

    protected Note() {}

    public Note(String content, User owner, Folder folder) {
        this.content = content;
        this.owner = owner;
        this.folder = folder;
    }

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

    public Folder getFolder() {
        return folder;
    }
}
