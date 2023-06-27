package io.github.dunwu.javadb.elasticsearch.springboot.entities;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@ToString
@Document(indexName = "user")
public class User {

    @Id
    private String id;

    private String userName;

    private int age;

    private String password;

    @Field(type = FieldType.Text, fielddata = true)
    private String email;

    public User() {}

    public User(String userName, int age, String password, String email) {
        this.userName = userName;
        this.age = age;
        this.password = password;
        this.email = email;
    }

    public User(String id, String userName, int age, String password, String email) {
        this.id = id;
        this.userName = userName;
        this.age = age;
        this.password = password;
        this.email = email;
    }

}
