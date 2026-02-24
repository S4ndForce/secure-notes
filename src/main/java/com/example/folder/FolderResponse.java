package com.example.folder;

import java.time.Instant;

public class FolderResponse {

    private Long id;
    private String name;
    private String userName;
    private Instant createdAt;
    private Instant updatedAt;

    public FolderResponse(Long id, String name, String userName, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUserName() { return userName; }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }

    public static FolderResponse fromEntity(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getOwner().getEmail(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}
