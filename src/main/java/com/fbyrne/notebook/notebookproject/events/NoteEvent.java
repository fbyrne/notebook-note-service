package com.fbyrne.notebook.notebookproject.events;

import com.fbyrne.notebook.notebookproject.model.Note;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class NoteEvent {

    @NonNull
    private String username;

    @NonNull
    private String email;

    @NonNull
    private Note note;

}
