package com.example.note;

import com.example.auth.OwnerAction;
import com.example.auth.OwnerAuthorization;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final OwnerAuthorization ownedAuth;
    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    public NoteService(NoteRepository noteRepository,
                       CurrentUser currentUser,
                       FolderRepository folderRepository,
                       SharedLinkService sharedLinkService,
                       TagRepository tagRepository, OwnerAuthorization ownedAuth
                       ) {
        this.noteRepository = noteRepository;
        this.currentUser = currentUser;
        this.folderRepository = folderRepository;
        this.sharedLinkService = sharedLinkService;
        this.tagRepository = tagRepository;
        this.ownedAuth = ownedAuth;
    }
    // Helper Specs
    /* ---------------------------------------------------------------------------------------------------*/
    private Specification<Note> ownedActive(Long id, User user) {
        return Specification
                .allOf(NoteSpecs.withId(id))
                .and(NoteSpecs.belongsTo(user))
                .and(NoteSpecs.notDeleted())
                .and(NoteSpecs.folderNotDeleted());
    }

    private Specification<Note> ownedDeleted(Long id, User user) {
        return Specification
                .allOf(NoteSpecs.withId(id))
                .and(NoteSpecs.belongsTo(user))
                .and(NoteSpecs.folderNotDeleted());

    }
    private Specification<Note> ownedActiveInFolder(Long folderId, User user) {
        return Specification
                .allOf(NoteSpecs.inFolder(folderId)) //implicit checking for folders
                .and(NoteSpecs.belongsTo(user))
                .and(NoteSpecs.notDeleted())
                .and(NoteSpecs.folderNotDeleted());
    }

    private Specification<Note> allActive(User user) {
        return Specification
                .allOf(NoteSpecs.belongsTo(user))
                .and(NoteSpecs.notDeleted())
                .and(NoteSpecs.folderNotDeleted());

    }
    /* ---------------------------------------------------------------------------------------------------*/

    // Business logic
    public NoteResponse create(Long folderId, String content, Authentication auth) {
        User user = currentUser.get(auth);
        Specification<Folder> spec = Specification.allOf(FolderSpecs.withId(folderId))
                .and(FolderSpecs.belongsTo(user))
                .and(FolderSpecs.notDeleted());
        Folder folder = folderRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        ownedAuth.authorize(OwnerAction.CREATE);
        Note note = new Note(content, user, folder);
        Instant now = Instant.now();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        noteRepository.save(note);
        return NoteResponse.fromEntity(note);


    }

    public NoteResponse getById(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Note> spec = ownedActive(id, user);
        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Note not found"));

        ownedAuth.authorize(OwnerAction.READ);
        return NoteResponse.fromEntity(note);

    }

    public List<NoteResponse> getByFolder(Long folderId, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Folder> folderSpec = Specification
                .allOf(FolderSpecs.withId(folderId))
                .and(FolderSpecs.belongsTo(user));

        Folder folder = folderRepository.findOne(folderSpec)
                .orElseThrow(() -> new NotFoundException("Not your folder"));

        Specification<Note> noteSpec = ownedActiveInFolder(folderId, user);

        List<Note> notes = noteRepository.findAll(noteSpec);
        ownedAuth.authorize(OwnerAction.READ);
        return notes.stream()
                .map(NoteResponse::fromEntity)
                .toList();
    }

    // Still uses classic repo logic
    // Refactor later to use specifications
    public List<NoteResponse> getMyFilteredNotes(Authentication auth) {
        User user = currentUser.get(auth);
        ownedAuth.authorize(OwnerAction.READ);
        return noteRepository.findByOwner(user)
                .stream()
                .map(NoteResponse::fromEntity)
                .toList();
    }

    public NoteResponse update(Long id, String content, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Note> spec = ownedActive(id,user);

        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Note not found"));

        ownedAuth.authorize(OwnerAction.UPDATE);

        note.setContent(content);
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);

        return NoteResponse.fromEntity(note);
    }

    public void delete(Long id, Authentication auth) {

        User user = currentUser.get(auth);

        Specification<Note> spec = ownedActive(id, user);

        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Note not found"));
        ownedAuth.authorize(OwnerAction.DELETE);
        note.setDeletedAt(Instant.now());
        noteRepository.save(note);
    }

    public void restore(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Note> spec = ownedDeleted(id, user);

        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Note not found"));
        ownedAuth.authorize(OwnerAction.UPDATE);
        note.setDeletedAt(null);
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);
    }



    public String createSharedLink(Long id, Long expiration, Authentication auth) {
        User user = currentUser.get(auth);


        Specification<Note> spec = ownedActive(id, user);

        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Note not found"));

        ownedAuth.authorize(OwnerAction.SHARE);

        note.setVisibility(Visibility.SHARED_LINK);
        noteRepository.save(note);

        SharedLink link = sharedLinkService.create(
                note,
                Set.of(SharedAction.READ),
                user,
                Instant.now().plusSeconds(expiration) // add configurability later
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

        Specification<Note> spec = allActive(user);

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
        ownedAuth.authorize(OwnerAction.READ);
        var content = notes.map(NoteResponse::fromEntity).toList();

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

        Specification<Note> spec = ownedActive(noteId, user);
        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Note not found"));
        ownedAuth.authorize(OwnerAction.UPDATE);
        // Normalization
        Set<Tag> tagSet = names.stream()
                .map(name -> name.trim().toLowerCase())
                .filter(name -> !name.isEmpty())
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toSet());

        note.getTags().addAll(tagSet);
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);

        return NoteResponse.fromEntity(note);
    }

    @Transactional
    public NoteResponse removeTag(Long noteId, String name, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Note> spec = ownedActive(noteId, user);
        Note note = noteRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Note not found"));
        ownedAuth.authorize(OwnerAction.UPDATE);

        tagRepository.findByName(name).ifPresent(t -> note.getTags().remove(t));
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);

        return NoteResponse.fromEntity(note);
    }





}
