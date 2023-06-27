package io.github.dunwu.javadb.h2.springboot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * JPA Rest 接口，对应 user 表
 * <p>
 * 启动 Application 后，直接访问：http://<host:ip>/user
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2019-10-12
 */
@RepositoryRestResource(collectionResourceRel = "user", path = "user")
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * <p>
     * 示例：http://localhost:8080/user/search/findByName?name=lisi
     * @param name 用户名
     * @return {@link User}
     */
    User findByName(@Param("name") String name);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return {@link User}
     */
    @Query("from User u where u.email=:email")
    User findByEmail(@Param("email") String email);

    /**
     * 根据用户名删除用户
     * @param name 用户名
     */
    void deleteByName(@Param("name") String name);

}
