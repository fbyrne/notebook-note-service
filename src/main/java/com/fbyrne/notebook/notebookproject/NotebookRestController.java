package com.fbyrne.notebook.notebookproject;

import com.fbyrne.notebook.notebookproject.model.Note;
import com.fbyrne.notebook.notebookproject.persistence.NotebookReactiveRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class NotebookRestController {

    private final NotebookReactiveRepository repository;

    public NotebookRestController(NotebookReactiveRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/note")
    Flux<Note> notesByOwner(@RequestParam Mono<UUID> owner) {
        return repository.findByOwner(owner);
    }

}
