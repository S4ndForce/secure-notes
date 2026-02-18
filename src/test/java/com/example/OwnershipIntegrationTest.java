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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private Note note;

    @BeforeEach
    void setup() {
        userA = userRepository.save(
                new User("a@test.com", passwordEncoder.encode("password"), Role.USER)
        );

        userB = userRepository.save(
                new User("b@test.com", passwordEncoder.encode("password"), Role.USER)
        );

        Folder folder = folderRepository.save(new Folder("Default", userA));
        note = noteRepository.save(
                new Note("Secret note", userA, folder)
        );
    }

    @Test
    void userCannotAccessAnotherUsersNote() throws Exception {

        mockMvc.perform(get("/notes/{id}", note.getId())
                        .with(user("b@test.com").password("password").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void ownerCanAccessOwnNote() throws Exception {

        mockMvc.perform(get("/notes/{id}", note.getId())
                        .with(user("a@test.com").password("password").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void ownerCannotAccessSoftDeletedNote() throws Exception {

        // soft delete first
        mockMvc.perform(delete("/notes/{id}", note.getId())
                        .with(user("a@test.com").password("password").roles("USER")))
                .andExpect(status().isNoContent());

        // try to retrieve delete note
        mockMvc.perform(get("/notes/{id}", note.getId())
                        .with(user("a@test.com").password("password").roles("USER")))
                .andExpect(status().isNotFound());
    }

}

