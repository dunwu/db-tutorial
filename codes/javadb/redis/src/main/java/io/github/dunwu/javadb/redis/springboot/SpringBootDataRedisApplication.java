package io.github.dunwu.javadb.redis.springboot;

import io.github.dunwu.javadb.redis.springboot.data.User;
import io.github.dunwu.javadb.redis.springboot.data.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootDataRedisApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringBootDataRedisApplication.class);

    private final UserService userService;

    public SpringBootDataRedisApplication(UserService userService) {
        this.userService = userService;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDataRedisApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        User user = new User(1L, "张三", 21, "南京", "xxx@163.com");
        User user2 = new User(2L, "李四", 28, "上海", "xxx@163.com");
        userService.setUser(user);
        userService.setUser(user2);

        User result = userService.getUser(user.getId());
        User result2 = userService.getUser(user2.getId());

        log.info(result.toString());
        log.info(result2.toString());
    }

}
