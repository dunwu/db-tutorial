package io.github.dunwu.javadb.h2.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class SpringBootDataJpaApplication implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final DataSource dataSource;

    public SpringBootDataJpaApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDataJpaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        if (dataSource != null) {
            printDataSourceInfo(dataSource);
            log.info("Connect to datasource success.");
        } else {
            log.error("Connect to datasource failed!");
        }
    }

    private void printDataSourceInfo(DataSource dataSource) throws SQLException {

        Connection connection;
        if (dataSource != null) {
            connection = dataSource.getConnection();
        } else {
            log.error("Get dataSource failed!");
            return;
        }

        if (connection != null) {
            log.info("DataSource Url: {}", connection.getMetaData().getURL());
        } else {
            log.error("Connect to datasource failed!");
        }
    }

}
