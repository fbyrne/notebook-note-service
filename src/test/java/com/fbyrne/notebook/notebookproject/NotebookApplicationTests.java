package com.fbyrne.notebook.notebookproject;

import com.fbyrne.notebook.notebookproject.model.Note;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/**
 * These tests are disabled as SpringSecurity OAuth2 does
 * not play well with Spring Security 5 Test Support.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-application-type=reactive"
        })
@ContextConfiguration(classes = NotebookApplicationTests.TestDependencyConfiguration.class)
@ActiveProfiles("test")
@Disabled
class NotebookApplicationTests {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @BeforeEach
    void authenticate(){
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        webTestClient = webTestClient.mutateWith(mockJwt());
    }

    @TestConfiguration
    static class TestDependencyConfiguration {
        @Bean
        ConnectionFactory connectionFactory() {
            return new CachingConnectionFactory(new MockConnectionFactory());
        }
    }

    @Test
    void test_create_and_retrieve_note() {
        Note note = newNote("My First Note!");
        Note savedNote = webTestClient.post()
                .uri("/note")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(note))
                .exchange()
                .expectStatus().isCreated().returnResult(Note.class).getResponseBody().blockFirst();

        webTestClient.get().uri("/note/{id}", savedNote.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Note.class);
    }

    private Note newNote(String content) {
        String owner = UUID.randomUUID().toString();
        Note note = new Note(content);
        note.setOwner(owner);
        return note;
    }

    @Test
    void test_as_a_user_i_can_list_my_notes() {
        Note note = newNote("My First Note!");

        FluxExchangeResult<Note> noteFluxExchangeResult = webTestClient.post()
                .uri("/note")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(note))
                .exchange()
                .expectStatus().isCreated().returnResult(Note.class);
        Note savedNote1 = noteFluxExchangeResult.getResponseBody().blockFirst();

        note.setContent("My Second Note!");
        noteFluxExchangeResult = webTestClient.post()
                .uri("/note")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(note))
                .exchange()
                .expectStatus().isCreated().returnResult(Note.class);
        Note savedNote2 = noteFluxExchangeResult.getResponseBody().blockFirst();

        webTestClient.get().uri("/note?owner={owner}", note.getOwner())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Note.class).hasSize(2);
    }

    @Test
    void test_as_a_user_i_can_update_my_notes() {
        Note note = newNote("My First Note!");
        EntityExchangeResult<Note> savedNoteResult = webTestClient.post()
                .uri("/note")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(note))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(Note.class)
                .returnResult();
        URI savedNoteLocation = savedNoteResult.getResponseHeaders().getLocation();
        Note savedNote1 = savedNoteResult.getResponseBody();
        savedNote1.setContent("My Updated Note!");

        webTestClient.put()
                .uri(savedNoteLocation.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(savedNote1))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Note.class)
                .value(result -> {
                    assertEquals(savedNote1.getId(), result.getId());
                    assertEquals(savedNote1.getCreated().truncatedTo(ChronoUnit.MILLIS), result.getCreated());
                    assertEquals(result.getContent(), result.getContent());
                    assertNotEquals(result.getCreated(), result.getModified());
                });

        Note savedNote2 = savedNoteResult.getResponseBody();
        webTestClient.get().uri("/note?owner={owner}", savedNote1.getOwner())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Note.class).hasSize(1);
    }


    @Test
    void test_as_a_user_i_can_delete_a_note_by_id() {
        Note note = newNote("My First Note!");
        EntityExchangeResult<Note> savedNoteResult = webTestClient.post()
                .uri("/note")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(note))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(Note.class)
                .returnResult();
        URI savedNoteLocation = savedNoteResult.getResponseHeaders().getLocation();
        Note savedNote1 = savedNoteResult.getResponseBody();

        webTestClient.delete()
                .uri(savedNoteLocation.getPath())
                .exchange()
                .expectStatus().isOk();

        webTestClient.get().uri(savedNoteLocation.getPath())
                .exchange()
                .expectStatus().isNotFound();
    }

}
