package com.example.note;

import com.example.user.User;
import org.springframework.data.jpa.domain.Specification;

public class NoteSpecs {

    public static Specification<Note> belongsTo(User user) {
        return (root, query, cb) -> cb.equal(root.get("owner"), user);
    }

    public static Specification<Note> withId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Note> contentContains(String text) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("content")), "%" + text.toLowerCase() + "%");
    }

    public static Specification<Note> inFolder(Long folderId) {
        return (root, query, cb) ->
                cb.equal(root.get("folder").get("id"), folderId);
    }



}