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
class NotebookControllerSecurityTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @BeforeEach
    void authenticate() {
        this.webTestClient = webTestClient.mutate().apply(csrf()).build();
    }

    @Test
    @WithAnonymousUser
    void test_unauthorized() {
        webTestClient
                .get().uri("/note")
                .exchange()
                .expectStatus().isForbidden();
    }
}