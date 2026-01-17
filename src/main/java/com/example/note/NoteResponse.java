package com.example.note;

import com.example.user.User;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

public class NoteResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private String userName;

    private Long folderId;

    private Instant createdAt;
    private Instant updatedAt;


    public NoteResponse(Long id, String content, String userName, Long folderId){
        this.content = content;
        this.userName = userName;
        this.id =  id;
        this.folderId = folderId;
    }
    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getUserName() {
        return userName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserName(User user) {
        userName = user.getEmail();
    }


    public static NoteResponse fromEntity(Note note){
        return new NoteResponse(
                note.getId(),
                note.getContent(),
                note.getOwner().getEmail(),
                note.getFolder().getId()

        );


    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
