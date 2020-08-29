package com.fbyrne.notebook.notebookproject.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static com.fbyrne.notebook.notebookproject.events.EventsConfiguration.*;


@Service
@CommonsLog
public class AmqpEventPublisher {

    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper;

    public AmqpEventPublisher(AmqpTemplate amqpTemplate, ObjectMapper objectMapper) {
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    @EventListener
    void postNoteCreatedEvent(NoteCreatedEvent event){
        sendEvent(event, NOTES_EVENT_CREATED_KEY);
    }

    @Async
    @EventListener
    void postNoteUpdatedEvent(NoteUpdatedEvent event){
        sendEvent(event, NOTES_EVENT_UPDATED_KEY);
    }

    @Async
    @EventListener
    void postNoteDeletedEvent(NoteDeletedEvent event){
        sendEvent(event, NOTES_EVENT_DELETED_KEY);
    }

    private void sendEvent(NoteEvent event, String routingKey) {
        try {
            String noteJson = this.objectMapper.writer().writeValueAsString(event.getNote());
            Message messageToSend = MessageBuilder.withBody(noteJson.getBytes(StandardCharsets.UTF_8))
                    .setContentType(MediaType.APPLICATION_JSON_VALUE)
                    .setHeader("username", event.getUsername())
                    .setHeader(StandardClaimNames.EMAIL, event.getEmail())
                    .build();
            this.amqpTemplate.convertAndSend(EXCHANGE_NAME, routingKey, messageToSend);
        } catch (Exception e) {
            log.error("unable to send created event", e);
        }
    }

}
