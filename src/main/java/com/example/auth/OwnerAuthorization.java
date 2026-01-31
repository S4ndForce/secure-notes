package com.example.auth;

import com.example.exceptions.ForbiddenException;
import com.example.note.Note;
import com.example.user.User;
import org.springframework.stereotype.Component;

@Component
public class OwnerAuthorization {

    public void authorize(OwnerAction action) {
        switch (action) {
            case READ, CREATE, UPDATE, DELETE, SHARE -> {
                // allowed for now
            }
            default -> throw new ForbiddenException("Action not allowed");
        }
    }
}
