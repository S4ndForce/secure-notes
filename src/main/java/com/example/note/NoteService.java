package com.example.note;

import com.example.user.User;
import com.example.auth.CurrentUser;
import com.example.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final CurrentUser currentUser;

    public NoteService(NoteRepository noteRepository, CurrentUser currentUser) {
        this.noteRepository = noteRepository;
        this.currentUser = currentUser;
    }

    public Note create(String content, Authentication auth) {
        User user = currentUser.get(auth);
        return noteRepository.save(new Note(content, user));
    }

    public Note getById(Long id, Authentication auth) {
        User user = currentUser.get(auth);
        Note note = noteRepository.findById(id).orElseThrow();

        if (!note.isOwnedBy(user)) {
            throw new RuntimeException("Forbidden");
        }

        return note;
    }
}
