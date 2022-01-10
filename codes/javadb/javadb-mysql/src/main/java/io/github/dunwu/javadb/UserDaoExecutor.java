package io.github.dunwu.javadb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import javax.annotation.PostConstruct;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-10-11
 */
@Slf4j
@Component
public class UserDaoExecutor {

    private final UserDao userDao;

    public UserDaoExecutor(UserDao userDao) {
        this.userDao = userDao;
    }

    @PostConstruct
    public void method() {
        if (userDao != null) {
            log.info("Connect to datasource success.");
        } else {
            log.error("Connect to datasource failed!");
            return;
        }

        List<User> list = userDao.list();
        list.forEach(item -> log.info(item.toString()));
    }

}
