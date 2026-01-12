package com.example.shared;

import com.example.note.NoteResponse;
import com.example.note.Note;
import com.example.exceptions.NotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shared")
public class SharedLinkController {

    private final SharedLinkRepository sharedLinkRepository;
    private final SharedLinkService sharedLinkService;

    public SharedLinkController(SharedLinkRepository sharedLinkRepository, SharedLinkService sharedLinkService) {
        this.sharedLinkRepository = sharedLinkRepository;
        this.sharedLinkService = sharedLinkService;
    }

    @GetMapping("/{token}")
    public NoteResponse getShared(@PathVariable String token) {
        SharedLink link = sharedLinkService.validate(token, SharedAction.READ);
        Note note = link.getNote();
        return NoteResponse.fromEntity(note);
    }
}
