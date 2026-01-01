package com.example.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
    // The only part that returns for now, returns a name of a previously created auth

    @GetMapping("/me")
    public String me(Authentication authentication) {
        return authentication.getName();
    }
}