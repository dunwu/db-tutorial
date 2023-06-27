package io.github.dunwu.javadb.elasticsearch.springboot.entities;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@ToString
public class GirlFriend {

    private String name;

    private String type;

    @Field(type = FieldType.Nested)
    private List<Car> cars;

}
