package com.fbyrne.notebook.notebookproject.endpoints;

import com.fbyrne.notebook.notebookproject.model.Note;
import com.fbyrne.notebook.notebookproject.persistence.NotebookReactiveRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class NotebookHandler {

    private final NotebookReactiveRepository repository;

    public NotebookHandler(NotebookReactiveRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> listNotes(ServerRequest request) {
        return request.queryParam("owner")
                .map(owner -> {
            Flux<Note> notes = this.repository.findByOwner(Mono.just(owner));
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(notes, Note.class);
        }).orElse(ServerResponse.ok().build());
    }

    public Mono<ServerResponse> getNotes(ServerRequest request) {
        return request.queryParam("owner")
                .map(owner -> {
                    Flux<Note> notes = this.repository.findByOwner(Mono.just(owner));
                    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                            .body(notes, Note.class);
                }).orElse(ServerResponse.ok().build());
    }

    public Mono<ServerResponse> createNote(ServerRequest request) {
        Mono<Note> noteToSave = request.bodyToMono(Note.class);
        return noteToSave
                .flatMap(this.repository::save)
                .flatMap(savedNote -> ServerResponse
                        .created(request.uriBuilder().pathSegment(savedNote.getId()).build())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedNote));
    }

    public Mono<ServerResponse> updateNote(ServerRequest request) {
        String noteId = request.pathVariable("id");
        Mono<Note> noteToSave = request.bodyToMono(Note.class);
        return repository.findById(noteId)
                .zipWith(noteToSave, (existingNote, updateNote) -> {
                    existingNote.setContent(updateNote.getContent());
                    return existingNote;
                })
                .flatMap(this.repository::save)
                .flatMap(savedNote -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savedNote))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteNote(ServerRequest request) {
        String noteId = request.pathVariable("id");

        return this.repository.deleteById(noteId)
                .then(ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}