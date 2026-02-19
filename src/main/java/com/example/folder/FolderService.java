package com.example.folder;

import com.example.auth.CurrentUser;
import com.example.auth.OwnerAction;
import com.example.auth.OwnerAuthorization;
import com.example.exceptions.NotFoundException;
import com.example.note.Note;
import com.example.note.NoteRepository;
import com.example.note.NoteSpecs;
import com.example.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final CurrentUser currentUser;
    private final NoteRepository noteRepository;
    private final OwnerAuthorization ownedAuth;
    // Helper Specs
    /* ---------------------------------------------------------------------------------------------------*/
    private Specification<Folder> ownedActiveFolder(Long id, User user) {
        return Specification
                .allOf(FolderSpecs.withId(id))
                .and(FolderSpecs.belongsTo(user))
                .and(FolderSpecs.notDeleted());
    }

    private Specification<Folder> ownedDeletedFolder(Long id, User user) {
        return Specification.
                allOf(FolderSpecs.withId(id))
                .and(FolderSpecs.belongsTo(user));
        // intentionally omits notDeleted() - targets soft-deleted folders only
    }

    private Specification<Folder> allActive(User user) {
        return Specification
                .allOf(FolderSpecs.belongsTo(user))
                .and(FolderSpecs.notDeleted());
    }
    /* ---------------------------------------------------------------------------------------------------*/

    public FolderService(FolderRepository folderRepository, CurrentUser currentUser, NoteRepository noteRepository, OwnerAuthorization ownedAuth) {
        this.folderRepository = folderRepository;
        this.currentUser = currentUser;
        this.noteRepository = noteRepository;
        this.ownedAuth = ownedAuth;
    }

    public FolderResponse create(String name, Authentication auth) {
        User user = currentUser.get(auth);
        ownedAuth.authorize(OwnerAction.CREATE);
        Folder folder = new Folder(name, user);
        Instant now = Instant.now();
        folder.setCreatedAt(now);
        folder.setUpdatedAt(now);
        folderRepository.save(folder);
        return FolderResponse.fromEntity(folder);
    }

    public List<FolderResponse> getMyFolders(Authentication auth) {
        User user = currentUser.get(auth);
        Specification<Folder> spec = allActive(user);
        List<Folder> folders = folderRepository.findAll(spec);
                return folders.stream()
                .map(FolderResponse::fromEntity)
                .toList();

    }


    public FolderResponse getById(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Folder> spec = ownedActiveFolder(id, user);

        Folder folder = folderRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        ownedAuth.authorize(OwnerAction.READ);
        return FolderResponse.fromEntity(folder);
    }

    public FolderResponse update(Long id, String name, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Folder> spec = ownedActiveFolder(id, user);

        Folder folder = folderRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Folder not found"));
        ownedAuth.authorize(OwnerAction.UPDATE);
        folder.setName(name);
        folderRepository.save(folder);
        return FolderResponse.fromEntity(folder);
    }


    @Transactional
    public void delete(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Folder> spec = ownedActiveFolder(id, user);
        ownedAuth.authorize(OwnerAction.DELETE);

        Folder folder = folderRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        Instant now = Instant.now();

        // 1. Soft-delete folder
        folder.setDeletedAt(now);

        // 2. Cascade soft-delete notes
        List<Note> notes = noteRepository.findAll(
                NoteSpecs.inFolder(folder.getId())
                        .and(NoteSpecs.notDeleted())
        );

        for (Note note : notes) {
            note.setDeletedAt(now);
            note.setCascadeDeletedAt(now);
            note.setUpdatedAt(now);
        }

        noteRepository.saveAll(notes);
        folderRepository.save(folder);
    }


    @Transactional
    public void restore(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        Specification<Folder> spec = ownedDeletedFolder(id, user);
        ownedAuth.authorize(OwnerAction.UPDATE);

        Folder folder = folderRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        Instant now = Instant.now();


        folder.setUpdatedAt(now);

        List<Note> notes = noteRepository.findAll(
                NoteSpecs.inFolder(folder.getId())
                        .and(NoteSpecs.isCascadeDeleted())
        );

        for (Note note : notes) {
                note.setDeletedAt(null);
                note.setCascadeDeletedAt(null);
                note.setUpdatedAt(now);
            }
        folder.setDeletedAt(null);
        noteRepository.saveAll(notes);
        folderRepository.save(folder);
    }
}
