package com.fbyrne.notebook.notebookproject.persistence;

import com.fbyrne.notebook.notebookproject.model.Note;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface NotebookReactiveRepository extends ReactiveMongoRepository<Note, UUID> {

    Flux<Note> findByOwner(Mono<UUID> owner);

}
