package com.example.note;

import com.example.exceptions.ForbiddenException;
import com.example.exceptions.NotFoundException;
import com.example.folder.Folder;
import com.example.folder.FolderRepository;
import com.example.shared.SharedLink;
import com.example.shared.SharedLinkRepository;
import com.example.user.User;
import com.example.auth.CurrentUser;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final CurrentUser currentUser;
    private final FolderRepository folderRepository;
    private final SharedLinkRepository sharedLinkRepository;

    public NoteService(NoteRepository noteRepository,
                       CurrentUser currentUser,
                       FolderRepository folderRepository,
                       SharedLinkRepository sharedLinkRepository) {
        this.noteRepository = noteRepository;
        this.currentUser = currentUser;
        this.folderRepository = folderRepository;
        this.sharedLinkRepository = sharedLinkRepository;
    }

    public NoteResponse create(Long folderId, String content, Authentication auth) {
        User user = currentUser.get(auth);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        if (!folder.isOwnedBy(user)) {
            throw new ForbiddenException("Not your folder");
        }

        Note note = new Note(content, user, folder);
        noteRepository.save(note);
        return NoteResponse.fromEntity(note);
    }

    public NoteResponse getById(Long id, Authentication auth) {
        User user = currentUser.get(auth);

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note not found"));

        if (!note.isOwnedBy(user)) {
            throw new ForbiddenException("Not your note");
        }

        if (!note.getFolder().isOwnedBy(user)) {
            throw new ForbiddenException("Folder does not belong to you");
        }

        return NoteResponse.fromEntity(note);
    }

    public List<NoteResponse> getMyNotes(Authentication auth) {
        User user = currentUser.get(auth);

        return noteRepository.findByOwner(user)
                .stream()
                .map(NoteResponse::fromEntity)
                .toList();
    }

    public NoteResponse update(Long id, String content,Authentication auth){
        User user = currentUser.get(auth);
        Note note = noteRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Note not found"));

       if(!note.isOwnedBy(user)){
           throw new ForbiddenException("Not your note");
       }

       note.setContent(content);
       noteRepository.save(note);

       return NoteResponse.fromEntity(note);
    }

    public void delete(Long id, Authentication auth){
        User user = currentUser.get(auth);
        Note note = noteRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Note not found"));

        if(!note.isOwnedBy(user)){
            throw new ForbiddenException("Not your note");
        }
        noteRepository.deleteById(id);
    }

    public List<NoteResponse> getByFolder(Long folderId, Authentication auth) {
        User user = currentUser.get(auth);

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        if (!folder.isOwnedBy(user)) {
            throw new ForbiddenException("Not your folder");
        }

        return noteRepository.findByFolder(folder)
                .stream()
                .map(NoteResponse::fromEntity)
                .toList();
    }

    public String createSharedLink(Long id, Authentication auth) {
        User user = currentUser.get(auth);
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note not found"));

        if (!note.isOwnedBy(user)) {
            throw new ForbiddenException("Not your note");
        }

        note.setVisibility(Visibility.SHARED_LINK);

        String token = UUID.randomUUID().toString();

        sharedLinkRepository.save(new SharedLink(token, note));
        noteRepository.save(note);

        return token;
    }

    public List<NoteResponse> searchMyNotes(
            String text,
            Long folderId,
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

        List<Note> notes = noteRepository.findAll(spec);

        return notes.stream()
                .map(NoteResponse::fromEntity)
                .toList();
    }



}
