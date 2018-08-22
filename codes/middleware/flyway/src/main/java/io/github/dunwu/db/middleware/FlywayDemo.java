package io.github.dunwu.db.middleware;

import org.flywaydb.core.Flyway;

public class FlywayDemo {
    public static void main(String[] args) {
        // Create the Flyway instance
        Flyway flyway = new Flyway();

        // Point it to the database
        flyway.setDataSource("jdbc:h2:file:./target/io/github/dunwu/db/middleware", "sa", null);

        // Start the migration
        flyway.migrate();
    }
}
