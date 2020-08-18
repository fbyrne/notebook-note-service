package com.fbyrne.notebook.notebookproject.persistence;

import com.fbyrne.notebook.notebookproject.NotebookProjectApplication;
import com.fbyrne.notebook.notebookproject.model.Note;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.startsWith;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NotebookReactiveRepositoryTest.TestConfiguration.class, NotebookReactiveMongoConfiguration.class})
@TestPropertySource(properties = {
        "embedded.mongodb.database=notebook",
        "spring.data.mongodb.uri=mongodb://${embedded.mongodb.host}:${embedded.mongodb.port}/${embedded.mongodb.database}"
    }
)
class NotebookReactiveRepositoryTest {

    @Autowired
    NotebookReactiveRepository repository;

    @Test
    void givenExample_whenFindAllWithExample_thenFindAllMatching() {
        UUID noteId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        LocalDateTime creationDateTime = LocalDateTime.now(Clock.tickMillis(ZoneId.of("UTC").normalized()));
        String noteContent = "My note";
        repository.save(new Note(noteId, ownerId, creationDateTime, noteContent)).block();
        ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("owner", startsWith());
        Example<Note> example = Example.of(new Note(noteId, ownerId, creationDateTime, noteContent), matcher);
        Flux<Note> noteFlux = repository.findAll(example);

        StepVerifier
                .create(noteFlux)
                .assertNext(note -> {
                    assertEquals(noteId, note.getId());
                    assertEquals(ownerId, note.getOwner());
                    assertEquals(creationDateTime, note.getCreated());
                    assertEquals(noteContent, note.getContent());
                })
                .expectComplete()
                .verify();
    }

    @EnableAutoConfiguration
    @Configuration
    static class TestConfiguration {
    }
}
