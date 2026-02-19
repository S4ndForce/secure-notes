package com.example.note;

import com.example.shared.SharedLink;
import com.example.tag.Tag;
import com.example.user.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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

    public static Specification<Note> notDeleted() {
        return (root, query, cb) ->
                cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Note> isDeleted() {
        return (root, query, cb) ->
                cb.isNotNull(root.get("deletedAt"));
    }
    public static Specification<Note> isNotCascadeDeleted() {
        return (root, query, cb) ->
                cb.isNull(root.get("cascadeDeletedAt"));
    }
    public static Specification<Note> isCascadeDeleted() {
        return (root, query, cb) ->
                cb.isNotNull(root.get("cascadeDeletedAt"));
    }

    public static Specification<Note> folderNotDeleted() {
        return (root, query, cb) ->
                cb.isNull(root.get("folder").get("deletedAt"));
    }

    public static Specification<Note> hasSharedLinks() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<SharedLink> subRoot = subquery.from(SharedLink.class);

            subquery.select(subRoot.get("note").get("id"))
                    .where(cb.equal(subRoot.get("note"), root));

            return cb.exists(subquery);
        };
    }


}