package com.example.folder;

import com.example.note.Note;
import com.example.user.User;
import org.springframework.data.jpa.domain.Specification;

public class FolderSpecs {
    public static Specification<Folder>  withId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Folder> belongsTo(User user) {
        return (root, query, cb) -> cb.equal(root.get("owner"), user);
    }
}
