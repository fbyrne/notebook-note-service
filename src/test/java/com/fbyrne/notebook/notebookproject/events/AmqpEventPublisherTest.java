package com.fbyrne.notebook.notebookproject.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbyrne.notebook.notebookproject.NotebookApplication;
import com.fbyrne.notebook.notebookproject.model.Note;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = {AmqpEventPublisherTest.TestDependencyConfiguration.class, AmqpEventPublisher.class})
class AmqpEventPublisherTest {

    @TestConfiguration
    static class TestDependencyConfiguration {

        @Bean
        ConnectionFactory connectionFactory() {
            return new CachingConnectionFactory(new MockConnectionFactory());
        }
    }

    @Autowired
    private AmqpEventPublisher amqpEventPublisher;

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @BeforeEach
    void declareExchanges(){
        amqpEventPublisher.declareExchangeAndBindings();
    }

    @Test
    void test_a_created_note_is_posted(){
        Note note = new Note("Tests");
        amqpEventPublisher.postNoteCreatedEvent(new NoteCreatedEvent("test", "test@test.com", note));
    }

}