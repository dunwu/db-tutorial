package io.github.dunwu.db;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Zhang Peng
 * @date 2019-03-05
 */
public class SqliteApplication implements CommandLineRunner {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SqliteApplication.class).run(args);
    }

    @Override
    public void run(String... args) {
        SqliteDemo.main(null);
    }
}
