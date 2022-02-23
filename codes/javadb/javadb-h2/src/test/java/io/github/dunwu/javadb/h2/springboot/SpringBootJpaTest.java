package io.github.dunwu.javadb.h2.springboot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot + JPA 基本 CRUD 测试
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2019-10-12
 */
@DataJpaTest
@ActiveProfiles({"test"})
public class SpringBootJpaTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserRepository repository;

    @BeforeEach
    public void before() {
        repository.deleteAll();
    }

    @Test
    public void insert() {
        User user = new User("张三", 18, "北京", "user1@163.com");
        repository.save(user);
        Optional<User> optional = repository.findById(user.getId());
        assertThat(optional).isNotNull();
        assertThat(optional.isPresent()).isTrue();
    }

    @Test
    public void batchInsert() {
        List<User> users = new ArrayList<>();
        users.add(new User("张三", 18, "北京", "user1@163.com"));
        users.add(new User("李四", 19, "上海", "user1@163.com"));
        users.add(new User("王五", 18, "南京", "user1@163.com"));
        users.add(new User("赵六", 20, "武汉", "user1@163.com"));
        repository.saveAll(users);

        long count = repository.count();
        assertThat(count).isEqualTo(4);

        List<User> list = repository.findAll();
        assertThat(list).isNotEmpty().hasSize(4);
        list.forEach(this::accept);
    }

    private void accept(User user) {log.info(user.toString());}

    @Test
    public void delete() {
        List<User> users = new ArrayList<>();
        users.add(new User("张三", 18, "北京", "user1@163.com"));
        users.add(new User("李四", 19, "上海", "user1@163.com"));
        users.add(new User("王五", 18, "南京", "user1@163.com"));
        users.add(new User("赵六", 20, "武汉", "user1@163.com"));
        repository.saveAll(users);

        repository.deleteByName("张三");
        assertThat(repository.findByName("张三")).isNull();

        repository.deleteAll();
        List<User> list = repository.findAll();
        assertThat(list).isEmpty();
    }

    @Test
    public void findAllInPage() {
        List<User> users = new ArrayList<>();
        users.add(new User("张三", 18, "北京", "user1@163.com"));
        users.add(new User("李四", 19, "上海", "user1@163.com"));
        users.add(new User("王五", 18, "南京", "user1@163.com"));
        users.add(new User("赵六", 20, "武汉", "user1@163.com"));
        repository.saveAll(users);

        PageRequest pageRequest = PageRequest.of(1, 2);
        Page<User> page = repository.findAll(pageRequest);
        assertThat(page).isNotNull();
        assertThat(page.isEmpty()).isFalse();
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);

        List<User> list = page.get().collect(Collectors.toList());
        System.out.println("user list: ");
        list.forEach(System.out::println);
    }

    @Test
    public void update() {
        User oldUser = new User("张三", 18, "北京", "user1@163.com");
        oldUser.setName("张三丰");
        repository.save(oldUser);

        User newUser = repository.findByName("张三丰");
        assertThat(newUser).isNotNull();
    }

}
