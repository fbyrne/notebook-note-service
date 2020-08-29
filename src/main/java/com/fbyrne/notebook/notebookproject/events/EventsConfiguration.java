package com.fbyrne.notebook.notebookproject.events;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class EventsConfiguration {

    public static final String EXCHANGE_NAME = "notes";

    public static final String NOTES_CREATED_QUEUE = "notes-created-queue";
    public static final String NOTES_UPDATED_QUEUE = "notes-updated-queue";
    public static final String NOTES_DELETED_QUEUE = "notes-deleted-queue";
    public static final String NOTES_EVENT_CREATED_KEY = "notes.event.created";
    public static final String NOTES_EVENT_UPDATED_KEY = "notes.event.updated";
    public static final String NOTES_EVENT_DELETED_KEY = "notes.event.deleted";

    @Bean
    TopicExchange notesExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }


    @Bean
    Queue notesCreatedQueue() {
        return new Queue(NOTES_CREATED_QUEUE);
    }

    @Bean
    Binding notesCreatedBinding(Queue notesCreatedQueue, TopicExchange notesExchange) {
        return BindingBuilder.bind(notesCreatedQueue).to(notesExchange).with(NOTES_EVENT_CREATED_KEY);
    }


    @Bean
    Queue notesUpdatedQueue() {
        return new Queue(NOTES_UPDATED_QUEUE);
    }

    @Bean
    Binding notesUpdatedBinding(Queue notesUpdatedQueue, TopicExchange notesExchange) {
        return BindingBuilder.bind(notesUpdatedQueue).to(notesExchange).with(NOTES_EVENT_UPDATED_KEY);
    }

    @Bean
    Queue notesDeletedQueue() {
        return new Queue(NOTES_DELETED_QUEUE);
    }

    @Bean
    Binding notesDeletedBinding(Queue notesDeletedQueue, TopicExchange notesExchange) {
        return BindingBuilder.bind(notesDeletedQueue).to(notesExchange).with(NOTES_EVENT_DELETED_KEY);
    }
}
