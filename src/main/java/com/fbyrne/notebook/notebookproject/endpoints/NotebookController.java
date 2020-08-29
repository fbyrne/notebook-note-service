package com.fbyrne.notebook.notebookproject.endpoints;

import com.fbyrne.notebook.notebookproject.events.NoteCreatedEvent;
import com.fbyrne.notebook.notebookproject.events.NoteDeletedEvent;
import com.fbyrne.notebook.notebookproject.events.NoteUpdatedEvent;
import com.fbyrne.notebook.notebookproject.model.Note;
import com.fbyrne.notebook.notebookproject.persistence.NotebookReactiveRepository;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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

    @GetMapping("/note/{noteId}")
    public Mono<Note> getNote(@PathVariable String noteId, @AuthenticationPrincipal Jwt jwt) {
        return repository.findById(noteId);
    }

    @PostMapping("/note")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Note> createNote(@RequestBody Mono<Note> noteToSave, @AuthenticationPrincipal Jwt jwt, ServerHttpResponse response) {
        return noteToSave
            .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "note must be passed in body")))
            .flatMap(note -> {
                if (note.getId() != null) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "note must not have an 'id', this is generated"));
                }
                return Mono.just(note);
            })
            .map(n -> {
                n.setOwner(usernameFromJwt(jwt));
                return n;
            })
            .flatMap(this.repository::save)
            .map(note -> {
                response.getHeaders().add(HttpHeaders.LOCATION, "/note/" + note.getId());
                return note;
            })
            .doOnSuccess(note -> this.publisher.publishEvent(
                    new NoteCreatedEvent(usernameFromJwt(jwt), emailFromJwt(jwt), note))
            );
    }

    private String emailFromJwt(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaimAsString(StandardClaimNames.EMAIL);
    }

    private String usernameFromJwt(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaimAsString(USER_JWT_CLAIM);
    }

    @PutMapping("/note/{noteId}")
    public Mono<Note> updateNote(@PathVariable String noteId, @RequestBody Mono<Note> noteToSave, @AuthenticationPrincipal Jwt jwt) {
        return repository.findById(noteId)
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .zipWith(noteToSave, (existingNote, updateNote) -> {
                    /*
                     * XXX There is a critical section between this check and save.
                     */
                    if(existingNote.getVersion() != updateNote.getVersion()){
                        throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "trying to update from an outdated version");
                    }
                    existingNote.setContent(updateNote.getContent());
                    return existingNote;
                })
                .flatMap(this.repository::save)
                .doOnSuccess(note -> this.publisher.publishEvent(
                        new NoteUpdatedEvent(usernameFromJwt(jwt), emailFromJwt(jwt), note)));
    }

    @DeleteMapping("/note/{noteId}")
    public Mono<Void> deleteNote(@PathVariable String noteId, @AuthenticationPrincipal Jwt jwt) {
        return this.repository.findById(noteId)
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(note ->
                        this.repository.deleteById(noteId)
                                .doOnSuccess(v -> this.publisher.publishEvent(
                                        new NoteDeletedEvent(usernameFromJwt(jwt), emailFromJwt(jwt), note))));

    }
}