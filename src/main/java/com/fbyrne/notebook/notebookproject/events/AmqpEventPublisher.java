package com.fbyrne.notebook.notebookproject.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.amqp.core.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;


@Service
@CommonsLog
public class AmqpEventPublisher {

    public static final String NOTES_CREATED_QUEUE = "notes-created-queue";
    public static final String NOTES_UPDATED_QUEUE = "notes-updated-queue";
    public static final String NOTES_DELETED_QUEUE = "notes-deleted-queue";
    public static final String NOTES_EVENT_CREATED_KEY = "notes.event.created";
    public static final String NOTES_EVENT_UPDATED_KEY = "notes.event.updated";
    public static final String NOTES_EVENT_DELETED_KEY = "notes.event.deleted";
    public static final String EXCHANGE_NAME = "notes";

    private final AmqpAdmin amqpAdmin;
    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper;

    public AmqpEventPublisher(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate, ObjectMapper objectMapper) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = objectMapper;
    }

    @EventListener
    void initializeExchange(ApplicationReadyEvent ready){
        declareExchangeAndBindings();
    }

    protected void declareExchangeAndBindings() {
        log.info("Declaring exchange " + EXCHANGE_NAME);
        Exchange notesExchange = ExchangeBuilder.topicExchange(EXCHANGE_NAME)
                .durable(true)
                .build();
        amqpAdmin.declareExchange(notesExchange);

        log.info("Declaring queue " + NOTES_CREATED_QUEUE);
        Queue notesCreatedQueue = QueueBuilder.durable(NOTES_CREATED_QUEUE).build();
        amqpAdmin.declareQueue(notesCreatedQueue);
        amqpAdmin.declareBinding(BindingBuilder.bind(notesCreatedQueue)
                .to(notesExchange)
                .with(NOTES_EVENT_CREATED_KEY)
                .noargs()
        );

        log.info("Declaring queue " + NOTES_UPDATED_QUEUE);
        Queue notesUpdatedQueue = QueueBuilder.durable(NOTES_UPDATED_QUEUE).build();
        amqpAdmin.declareQueue(notesUpdatedQueue);
        amqpAdmin.declareBinding(BindingBuilder.bind(notesUpdatedQueue)
                .to(notesExchange)
                .with(NOTES_EVENT_UPDATED_KEY)
                .noargs()
        );

        log.info("Declaring queue " + NOTES_DELETED_QUEUE);
        Queue notesDeletedQueue = QueueBuilder.durable(NOTES_DELETED_QUEUE).build();
        amqpAdmin.declareQueue(notesDeletedQueue);
        amqpAdmin.declareBinding(BindingBuilder.bind(notesDeletedQueue)
                .to(notesExchange)
                .with(NOTES_EVENT_DELETED_KEY)
                .noargs()
        );
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
