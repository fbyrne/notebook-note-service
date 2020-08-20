package com.fbyrne.notebook.notebookproject.endpoints;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Objects;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class NotebookRouter {

    @Bean
    public RouterFunction<ServerResponse> route(NotebookHandler noteHandler) {
        return RouterFunctions
                .route(GET("/note").and(queryParam("owner", Objects::nonNull)), noteHandler::listNotes)
                .andRoute(GET("/note/{id}"),noteHandler::getNote)
                .andRoute(POST("/note").and(accept(MediaType.APPLICATION_JSON)),noteHandler::createNote)
                .andRoute(PUT("/note/{id}").and(accept(MediaType.APPLICATION_JSON)),noteHandler::updateNote)
                .andRoute(DELETE("/note/{id}"),noteHandler::deleteNote);
    }
}
