package io.github.dunwu.javadb.elasticsearch.springboot.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "operation")
public class Operation {

    @Id
    private Long id;

    @Field(
        type = FieldType.Text,
        searchAnalyzer = "standard",
        analyzer = "standard",
        store = true
    )
    private String operationName;

    @Field(
        type = FieldType.Date,
        index = false,
        store = true,
        format = DateFormat.custom,
        pattern = "yyyy-MM-dd hh:mm:ss"
    )
    private String dateUp;

    @Field(
        type = FieldType.Text,
        index = false,
        store = false
    )
    private String someTransientData;

    @Field(type = FieldType.Nested)
    private List<Sector> sectors;

}

