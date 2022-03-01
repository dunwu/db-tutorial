package io.github.dunwu.javadb.elasticsearch.springboot.entities;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@ToString
@Document(indexName = "product")
public class Product {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Boolean)
    private boolean enabled;

    public Product(String id, String name, String description, boolean enabled) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public Product() {}

}
