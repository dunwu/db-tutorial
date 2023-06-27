package io.github.dunwu.javadb.mysql.springboot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

@Slf4j
@SpringBootApplication
public class SpringBootDataJdbcApplication implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public SpringBootDataJdbcApplication(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDataJdbcApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        DataSource dataSource = jdbcTemplate.getDataSource();

        Connection connection;
        if (dataSource != null) {
            connection = dataSource.getConnection();
        } else {
            log.error("连接数据源失败！");
            return;
        }

        if (connection != null) {
            log.info("数据源 Url: {}", connection.getMetaData().getURL());
        } else {
            log.error("连接数据源失败！");
        }
    }

}
