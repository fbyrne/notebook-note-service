package com.fbyrne.notebook.notebookproject.persistence;

import com.fbyrne.notebook.notebookproject.model.Note;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
class NotebookReactiveRepositoryTest {

    @Autowired
    NotebookReactiveRepository repository;

    @Test
    void test_as_a_user_I_can_save_a_note() {
        String ownerId = UUID.randomUUID().toString();
        String noteContent = "My note";
        Note note1 = new Note(noteContent);
        note1.setOwner(ownerId);
        repository.save(note1).block();
        Flux<Note> noteFlux = repository.findByOwner(Mono.just(ownerId));

        StepVerifier
                .create(noteFlux)
                .assertNext(note -> {
                    assertNotNull(note.getId());
                    assertEquals(ownerId, note.getOwner());
                    assertNotNull(note.getCreated());
                    assertNotNull(note.getContent());
                })
                .expectComplete()
                .verify();
    }
}
