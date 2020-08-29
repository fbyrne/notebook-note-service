package com.fbyrne.notebook.notebookproject.endpoints;

import com.fbyrne.notebook.notebookproject.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.fbyrne.notebook.notebookproject.endpoints.NotebookController.USER_JWT_CLAIM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = NotebookController.class)
@ContextConfiguration
@AutoConfigureDataMongo
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NotebookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @BeforeEach
    void authenticate() {
        this.webTestClient = webTestClient.mutate()
                .apply(csrf())
                .apply(mockJwt().jwt(builder -> {
                    builder.claim(USER_JWT_CLAIM, "test");
                    builder.claim(StandardClaimNames.EMAIL, "test@test.com");
                })).build();
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
        webTestClient.post()
                .uri("/note")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(note))
                .exchange()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectStatus().isCreated();

        note.setContent("My Second Note!");
        webTestClient.post()
                .uri("/note")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(note))
                .exchange()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectStatus().isCreated();

        webTestClient.get().uri("/note")
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
        webTestClient.get().uri("/note")
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

        webTestClient.delete()
                .uri(savedNoteLocation.getPath())
                .exchange()
                .expectStatus().isNotFound();

        webTestClient.put()
                .uri(savedNoteLocation.getPath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(savedNote1))
                .exchange()
                .expectStatus().isNotFound();
    }
}