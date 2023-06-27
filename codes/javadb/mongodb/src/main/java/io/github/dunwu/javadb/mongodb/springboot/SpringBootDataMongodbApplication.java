package io.github.dunwu.javadb.mongodb.springboot;

import io.github.dunwu.javadb.mongodb.springboot.customer.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class SpringBootDataMongodbApplication implements CommandLineRunner {

    @Autowired
    private CustomerRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDataMongodbApplication.class, args);
    }

    @Override
    public void run(String... args) {
        //
        // repository.deleteAll();
        //
        // // save a couple of customers
        // repository.save(new Customer("Alice", "Smith"));
        // repository.save(new Customer("Bob", "Smith"));
        //
        // // fetch all customers
        // log.info("Customers found with findAll():");
        // log.info("-------------------------------");
        // for (Customer custom : repository.findAll()) {
        //     log.info(custom.toString());
        // }
        //
        // // fetch an individual customer
        // log.info("Customer found with findByFirstName('Alice'):");
        // log.info("--------------------------------");
        // log.info(repository.findByLastname("Alice", Sort.by("firstname")).toString());
        //
        // log.info("Customers found with findByLastName('Smith'):");
        // log.info("--------------------------------");
        // for (Customer custom : repository.findByLastname("Smith", Sort.by("firstname"))) {
        //     log.info(custom.toString());
        // }
    }

}
