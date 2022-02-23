package io.github.dunwu.javadb.redis.springboot.data;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 4142994984277644695L;

    private Long id;

    private String name;

    private Integer age;

    private String address;

    private String email;

    public User() {
    }

    public User(String name, Integer age, String address, String email) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.email = email;
    }

    public User(Long id, String name, Integer age, String address, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.address = address;
        this.email = email;
    }

}
