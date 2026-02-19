package com.example;

import com.example.folder.Folder;
import com.example.folder.FolderRepository;
import com.example.note.Note;
import com.example.note.NoteRepository;
import com.example.user.Role;
import com.example.user.User;
import com.example.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SoftDeleteIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    User userA;
    Folder folder;
    Note note, note1, note2;

    @BeforeEach
    void setup() {
        userA = userRepository.save(
                new User("userA", passwordEncoder.encode("password"), Role.USER)
        );

        folder = folderRepository.save(
                new Folder("Default", userA)
        );

        note = noteRepository.save(
                new Note("Secret", userA, folder)
        );

        note1 = noteRepository.save(
                new Note("Hidden", userA, folder)
        );

        note2 = noteRepository.save(
                new Note("Classified", userA, folder)
        );

    }
    @Test
    void ownerCanSoftDeleteNote() throws Exception {
        mockMvc.perform(delete("/notes/{id}", note.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notes/{id}", note.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void folderDeleteCascadesToNotes() throws Exception {

        mockMvc.perform(delete("/folders/{id}", folder.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notes/{id}", note1.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/notes/{id}", note2.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void folderRestoreDoesNotRestoreIndividuallyDeletedNotes() throws Exception {

        // Individually delete note
        mockMvc.perform(delete("/notes/{id}", note.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        // Delete folder
        mockMvc.perform(delete("/folders/{id}", folder.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        // Restore folder
        mockMvc.perform(post("/folders/{id}/restore", folder.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        // Note should still be inaccessible
        mockMvc.perform(get("/notes/{id}", note.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void restoringFolderRestoresCascadeDeletedNotes() throws Exception {

        mockMvc.perform(delete("/folders/{id}", folder.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/folders/{id}/restore", folder.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notes/{id}", note1.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notes/{id}", note2.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void cascadeDeletedNotesAreInvisible() throws Exception {
        mockMvc.perform(delete("/folders/{id}", folder.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notes/{id}", note.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void noteRestoreDoesNotRestoreCascadeDeletedNotes() throws Exception {
        mockMvc.perform(delete("/folders/{id}", folder.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/notes/{id}/restore", note.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void sharingCascadeDeletedNoteIsRejected() throws Exception {
        mockMvc.perform(delete("/folders/{id}", folder.getId())
                        .with(user("userA").roles("USER")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/notes/{id}/share", note.getId())
                        .with(user("userA").roles("USER"))
                        .contentType("application/json")
                        .content("""
            {
                "expiresInSeconds": 100
            }
        """))
                .andExpect(status().isNotFound());
    }
}
