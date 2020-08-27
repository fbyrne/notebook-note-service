package com.fbyrne.notebook.notebookproject.events;

import com.fbyrne.notebook.notebookproject.model.Note;
import lombok.NonNull;

public class NoteDeletedEvent extends NoteEvent {

    public NoteDeletedEvent(@NonNull String username, @NonNull String email, @NonNull Note note) {
        super(username, email, note);
    }
}
