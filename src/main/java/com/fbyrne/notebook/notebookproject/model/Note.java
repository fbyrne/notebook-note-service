package com.fbyrne.notebook.notebookproject.model;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class Note {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @CreatedBy
    private String owner;

    @CreatedDate
    private Instant created;

    @LastModifiedDate
    private Instant modified;

    @NonNull
    private String content;

}
