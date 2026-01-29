package com.example.auth;

import com.example.exceptions.ForbiddenException;
import com.example.note.Note;
import com.example.user.User;
import org.springframework.stereotype.Component;

@Component
public class OwnerAuthorization {

    public void authorize(OwnerAction action) {

        // IMPORTANT:
        // For now, ownership implies all actions are allowed.
        // We are making power explicit, not changing behavior.
    }
}
