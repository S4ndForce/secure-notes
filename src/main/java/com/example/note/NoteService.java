package com.example.note;

import com.example.exceptions.ForbiddenException;
import com.example.exceptions.NotFoundException;
import com.example.folder.Folder;
import com.example.folder.FolderRepository;
import com.example.folder.FolderSpecs;
import com.example.shared.SharedAction;
import com.example.shared.SharedLink;
import com.example.shared.SharedLinkService;
import com.example.tag.Tag;
import com.example.tag.TagRepository;
import com.example.user.User;
import com.example.auth.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final CurrentUser currentUser;
    private final FolderRepository folderRepository;
    private final SharedLinkService sharedLinkService;
    private final TagRepository tagRepository;

    public NoteService(NoteRepository noteRepository,
                       CurrentUser currentUser,
                       FolderRepository folderRepository,
                       SharedLinkService sharedLinkService,
                       TagRepository tagRepository) {
        this.noteRepository = noteRepository;
        this.currentUser = currentUser;
        this.folderRepository = folderRepository;
        this.sharedLinkService = sharedLinkService;
        this.tagRepository = tagRepository;
    }
    // Helper methods
    private Specification<Note> ownedNote(Long id, User user) {
        return Specification.allOf(NoteSpecs.withId(id)).and(NoteSpecs.belongsTo(user));
    }

    // Business logic

    public NoteResponse create(Long folderId, String content, Authentication auth) {
        User user = currentUser.get(auth);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        if (!folder.isOwnedBy(user)) {
            throw new ForbiddenException("Not your folder");
        }

        Note note = new Note(content, user, folder);
        Instant now = Instant.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        noteRepository.save(note);
        return NoteResponse.fromEntity(note);


    }



    public NoteResponse getById(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        boolean exists = noteRepository.existsById(id);
        if (!exists) {
            throw new NotFoundException("Note not found");
        }
        Specification<Note> spec = Specification
                .allOf(NoteSpecs.withId(id))
                .and(NoteSpecs.belongsTo(user));

        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new ForbiddenException("Not your note"));

        return NoteResponse.fromEntity(note);
    }

    public List<NoteResponse> getByFolder(Long folderId, Authentication auth) {
        User user = currentUser.get(auth);

        boolean exists = folderRepository.existsById(folderId);
        if (!exists) {
            throw new NotFoundException("Folder not found");
        }

        Specification<Folder> folderSpec = Specification
                .allOf(FolderSpecs.withId(folderId))
                .and(FolderSpecs.belongsTo(user));

        Folder folder = folderRepository.findOne(folderSpec)
                .orElseThrow(() -> new ForbiddenException("Not your folder"));

        Specification<Note> noteSpec = ownedNote(folderId, user);

        List<Note> notes = noteRepository.findAll(noteSpec);

        return notes.stream()
                .map(NoteResponse::fromEntity)
                .toList();
    }

    // Still uses classic repo logic
    // Refactor later to use specifications
    public List<NoteResponse> getMyFilteredNotes(Authentication auth) {
        User user = currentUser.get(auth);

        return noteRepository.findByOwner(user)
                .stream()
                .map(NoteResponse::fromEntity)
                .toList();
    }

    public NoteResponse update(Long id, String content, Authentication auth) {
        User user = currentUser.get(auth);


        if (!noteRepository.existsById(id)) {
            throw new NotFoundException("Note not found");
        }


        Specification<Note> spec = ownedNote(id,user);

        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new ForbiddenException("Not your note"));


        note.setContent(content);
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);

        return NoteResponse.fromEntity(note);
    }

    public void delete(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        if (!noteRepository.existsById(id)) {
            throw new NotFoundException("Note not found");
        }

        Specification<Note> spec = ownedNote(id, user);

        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new ForbiddenException("Not your note"));

        noteRepository.delete(note);
    }



    public String createSharedLink(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        if (!noteRepository.existsById(id)) {
            throw new NotFoundException("Note not found");
        }

        Specification<Note> spec = ownedNote(id, user);

        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new ForbiddenException("Not your note"));

        note.setVisibility(Visibility.SHARED_LINK);
        noteRepository.save(note);

        SharedLink link = sharedLinkService.create(
                note,
                Set.of(SharedAction.READ),
                null
        );

        return link.getToken();
    }

    public PageResponse<NoteResponse> searchMyNotes(
            String text,
            Long folderId,
            Pageable pageable,
            String tagName,
            Authentication auth
    ) {
        User user = currentUser.get(auth);

        Specification<Note> spec = Specification.allOf(NoteSpecs.belongsTo(user));

        if (text != null && !text.isBlank()) {
            spec = spec.and(NoteSpecs.contentContains(text));
        }

        if (folderId != null) {
            spec = spec.and(NoteSpecs.inFolder(folderId));
        }

        if (tagName != null && !tagName.isBlank()) {
            spec = spec.and(NoteSpecs.hasTag(tagName));
        }

        Page<Note> notes = noteRepository.findAll(spec, pageable);
        var content = notes.map(NoteResponse::fromEntity).toList()
                ;

        return new PageResponse<NoteResponse>(
                content,
                notes.getNumber(),
                notes.getSize(),
                notes.getTotalElements(),
                notes.getTotalPages()
        );
    }

    @Transactional
    public NoteResponse addTags(Long noteId, Set<String> names, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Note> spec = ownedNote(noteId, user);
        Note note = noteRepository.findOne(spec).orElseThrow(() -> new ForbiddenException("Not your note"));

        Set<Tag> tagSet = names.stream()
                .map(n -> tagRepository.findByName(n).orElseGet(() -> tagRepository.save(new Tag(n))))
                .collect(Collectors.toSet());

        note.getTags().addAll(tagSet);
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);

        return NoteResponse.fromEntity(note);
    }

    @Transactional
    public NoteResponse removeTag(Long noteId, String name, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Note> spec = ownedNote(noteId, user);
        Note note = noteRepository.findOne(spec).orElseThrow(() -> new ForbiddenException("Not your note"));

        tagRepository.findByName(name).ifPresent(t -> note.getTags().remove(t));
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);

        return NoteResponse.fromEntity(note);
    }



}
