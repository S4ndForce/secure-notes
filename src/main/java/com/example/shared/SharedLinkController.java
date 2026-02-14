package com.example.shared;

import com.example.note.NoteResponse;
import com.example.note.Note;
import com.example.exceptions.NotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shared")
public class SharedLinkController {

    private final SharedLinkService sharedLinkService;

    public SharedLinkController(SharedLinkService sharedLinkService) {

        this.sharedLinkService = sharedLinkService;
    }

    // Creation handled in NoteService

    @GetMapping("/{token}")
    public NoteResponse getShared(@PathVariable String token) {
        return sharedLinkService.getNote(token);
    }

    @PatchMapping("/{token}")
    public NoteResponse update(
            @PathVariable String token,
            @RequestBody SharedLinkUpdateRequest request
    ) {
        return sharedLinkService.updateViaSharedLink(token, request);
    }
}
