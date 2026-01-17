package com.example.note;

import com.example.tag.Tag;
import com.example.user.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class NoteSpecs {
    // QLA specs
    public static Specification<Note> belongsTo(User user) {
        return (root, query, cb) -> cb.equal(root.get("owner"), user);
    }

    public static Specification<Note> withId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }


    // Search specs

    public static Specification<Note> inFolder(Long folderId) {
        return (root, query, cb) ->
                cb.equal(root.get("folder").get("id"), folderId);
    }

    public static Specification<Note> contentContains(String text) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("content")), "%" + text.toLowerCase() + "%");
    }

    public static Specification<Note> hasTag(String name) {
        return (root, query, cb) -> {
            Join<Note, Tag> tags = root.join("tags", JoinType.INNER);
            return cb.equal(tags.get("name"), name);
        };
    }



}