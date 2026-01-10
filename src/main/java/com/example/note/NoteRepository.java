package com.example.note;

import com.example.folder.Folder;
import com.example.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {
    List<Note> findByOwner(User owner);
    List<Note> findByFolder(Folder folder);
}
