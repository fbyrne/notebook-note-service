package com.fbyrne.notebook.notebookproject.persistence;

import com.fbyrne.notebook.notebookproject.model.Note;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@ActiveProfiles("test")
@ContextConfiguration(classes = {NotebookReactiveRepositoryTest.TestDependencyConfiguration.class})
class NotebookReactiveRepositoryTest {

    @TestConfiguration
    @EnableReactiveMongoRepositories
    @EnableMongoAuditing
    static class TestDependencyConfiguration {
    }

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
