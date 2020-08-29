package com.fbyrne.notebook.notebookproject.events;

import com.fbyrne.notebook.notebookproject.model.Note;
import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = {AmqpEventPublisherTest.TestDependencyConfiguration.class, EventsConfiguration.class, AmqpEventPublisher.class})
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

    @Test
    void test_a_created_note_is_posted(){
        Note note = new Note("Tests");
        amqpEventPublisher.postNoteCreatedEvent(new NoteCreatedEvent("test", "test@test.com", note));

    }

}