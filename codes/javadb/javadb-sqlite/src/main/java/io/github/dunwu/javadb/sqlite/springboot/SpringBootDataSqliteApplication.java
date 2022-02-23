package io.github.dunwu.javadb.sqlite.springboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Zhang Peng
 * @since 2019-03-05
 */
@Slf4j
@SpringBootApplication
public class SpringBootDataSqliteApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDataSqliteApplication.class, args);
    }

    @Override
    public void run(String... args) {
        SqliteDemo.main(null);
    }

}
