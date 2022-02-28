package io.github.dunwu.javadb.elasticsearch.springboot.entities;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
@Document(indexName = "book")
public class Book {

    @Id
    private String id;

    private String name;

    private Long price;

    @Version
    private Long version;

    @Field(type = FieldType.Nested)
    private Map<Integer, Collection<String>> buckets = new HashMap<>();

    public Book() {}

    public Book(String id, String name, Long version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }

}
