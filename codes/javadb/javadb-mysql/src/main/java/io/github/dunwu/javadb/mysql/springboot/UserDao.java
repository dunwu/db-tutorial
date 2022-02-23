package io.github.dunwu.javadb.mysql.springboot;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * user 表 Dao 接口
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2019-11-18
 */
public interface UserDao {

    // DML
    // -------------------------------------------------------------------
    void insert(User user);

    void batchInsert(List<User> users);

    void deleteByName(String name);

    void deleteAll();

    void update(User user);

    Integer count();

    List<User> list();

    User queryByName(String name);

    JdbcTemplate getJdbcTemplate();

    // DDL
    // -------------------------------------------------------------------
    void truncate();

    void recreateTable();

}
