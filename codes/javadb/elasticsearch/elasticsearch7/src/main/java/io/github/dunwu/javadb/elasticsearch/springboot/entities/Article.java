package io.github.dunwu.javadb.elasticsearch.springboot.entities;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@ToString
@Document(indexName = "article")
public class Article {

    @Id
    private String id;

    private String title;

    @MultiField(mainField = @Field(type = FieldType.Text), otherFields = {
        @InnerField(suffix = "untouched", type = FieldType.Text, store = true, index = false),
        @InnerField(suffix = "sort", type = FieldType.Text, store = true, analyzer = "keyword")})
    private List<String> authors = new ArrayList<>();

    @Field(type = FieldType.Integer, store = true)
    private List<Integer> publishedYears = new ArrayList<>();

    @Field(type = FieldType.Text, store = true)
    private Collection<String> tags = new ArrayList<>();

    private int score;

    public Article() {}

    public Article(String id) {
        this.id = id;
    }

}
