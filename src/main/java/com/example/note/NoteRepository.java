package com.example.note;

import com.example.folder.Folder;
import com.example.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByOwner(User owner);
    List<Note> findByFolder(Folder folder);
}
