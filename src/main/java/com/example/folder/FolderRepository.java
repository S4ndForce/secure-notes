package com.example.folder;

import com.example.note.Note;
import com.example.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> , JpaSpecificationExecutor<Folder> {
    List<Folder> findByOwner(User owner);
}