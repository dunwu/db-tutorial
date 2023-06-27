package io.github.dunwu.javadb.elasticsearch.springboot;

import io.github.dunwu.javadb.elasticsearch.springboot.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-02-23
 */
@Slf4j
@SpringBootApplication
public class SpringBootDataElasticsearchApplication implements CommandLineRunner {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private UserRepository repository;


    public static void main(String[] args) {
        SpringApplication.run(SpringBootDataElasticsearchApplication.class);
    }

    @Override
    public void run(String... args) {
        System.out.println("[index = user] 的文档数：" + repository.count());
    }

}
