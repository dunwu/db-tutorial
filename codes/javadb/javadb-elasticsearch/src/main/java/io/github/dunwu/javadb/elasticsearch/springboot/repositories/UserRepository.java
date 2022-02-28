package io.github.dunwu.javadb.elasticsearch.springboot.repositories;

import io.github.dunwu.javadb.elasticsearch.springboot.entities.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserRepository extends ElasticsearchRepository<User, String> {

    List<User> findByUserName(String UserName);

    User findByEmail(String email);

}
