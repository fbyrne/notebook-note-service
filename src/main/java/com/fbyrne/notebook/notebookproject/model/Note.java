package com.fbyrne.notebook.notebookproject.model;

import lombok.*;
import org.springframework.data.annotation.*;
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

    @Version
    private long version;

    @CreatedBy
    private String owner;

    @CreatedDate
    private Instant created;

    @LastModifiedDate
    private Instant modified;

    @NonNull
    private String content;

}
