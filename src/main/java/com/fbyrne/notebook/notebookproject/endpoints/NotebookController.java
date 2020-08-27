package com.fbyrne.notebook.notebookproject.endpoints;

import com.fbyrne.notebook.notebookproject.events.NoteCreatedEvent;
import com.fbyrne.notebook.notebookproject.events.NoteDeletedEvent;
import com.fbyrne.notebook.notebookproject.events.NoteUpdatedEvent;
import com.fbyrne.notebook.notebookproject.model.Note;
import com.fbyrne.notebook.notebookproject.persistence.NotebookReactiveRepository;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CommonsLog
public class NotebookController {

    public static final String USER_JWT_CLAIM = "username";
    private final NotebookReactiveRepository repository;
    private final ApplicationEventPublisher publisher;

    public NotebookController(NotebookReactiveRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @GetMapping("/note")
    public Flux<Note> listNotes(@AuthenticationPrincipal Jwt jwt) {
        String user = usernameFromJwt(jwt);
        return this.repository.findByOwner(Mono.just(user));
    }

    @GetMapping("/note/{id}")
    public Mono<Note> getNote(@PathVariable("id") String noteId, @AuthenticationPrincipal Jwt jwt) {
        return repository.findById(noteId);
    }

    @PostMapping("/note")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Note> createNote(@RequestBody Mono<Note> noteToSave, @AuthenticationPrincipal Jwt jwt, ServerHttpResponse response) {
        return noteToSave.map(n -> {
                n.setOwner(usernameFromJwt(jwt));
                return n;
            })
                .flatMap(this.repository::save)
                .doOnSuccess(note -> this.publisher.publishEvent(
                        new NoteCreatedEvent(usernameFromJwt(jwt),emailFromJwt(jwt), note))
                );
    }

    private String emailFromJwt(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaimAsString(StandardClaimNames.EMAIL);
    }

    private String usernameFromJwt(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaimAsString(USER_JWT_CLAIM);
    }

    @PutMapping("/note/{id}")
    public Mono<Note> updateNote(@PathVariable String noteId, @RequestBody Mono<Note> noteToSave, @AuthenticationPrincipal Jwt jwt) {
        return repository.findById(noteId)
                .zipWith(noteToSave, (existingNote, updateNote) -> {
                    existingNote.setContent(updateNote.getContent());
                    return existingNote;
                })
                .flatMap(this.repository::save)
                .doOnSuccess(note -> this.publisher.publishEvent(
                        new NoteUpdatedEvent(usernameFromJwt(jwt), emailFromJwt(jwt), note)));
    }

    @DeleteMapping("/note/{id}")
    public Mono<Void> deleteNote(@PathVariable String noteId, @AuthenticationPrincipal Jwt jwt) {
        return this.repository.findById(noteId)
                .flatMap(note ->
                        this.repository.deleteById(noteId)
                            .doOnSuccess(v -> this.publisher.publishEvent(
                                    new NoteDeletedEvent(usernameFromJwt(jwt), emailFromJwt(jwt), note))));

    }
}