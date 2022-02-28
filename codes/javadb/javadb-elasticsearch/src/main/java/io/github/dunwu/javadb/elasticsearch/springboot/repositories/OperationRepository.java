package io.github.dunwu.javadb.elasticsearch.springboot.repositories;

import io.github.dunwu.javadb.elasticsearch.springboot.entities.Operation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OperationRepository extends ElasticsearchRepository<Operation, Long> {
}
