package com.example.note;

import com.example.user.User;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class NoteResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private String userName;

    private Long folderId;

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
}
