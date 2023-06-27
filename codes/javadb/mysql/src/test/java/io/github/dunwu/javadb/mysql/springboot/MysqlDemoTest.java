package io.github.dunwu.javadb.mysql.springboot;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Mysql 测试例
 * @author Zhang Peng
 * @see https://dev.mysql.com/doc/connector-j/5.1/en/
 */
public class MysqlDemoTest {

    private static final String DB_HOST = "localhost";

    private static final String DB_PORT = "3306";

    private static final String DB_SCHEMA = "db_tutorial";

    private static final String DB_USER = "root";

    private static final String DB_PASSWORD = "root";

    private static Logger logger = LoggerFactory.getLogger(MysqlDemoTest.class);

    private static Statement statement;

    private static Connection connection;

    @BeforeAll
    public static void beforeClass() {
        try {
            final String DB_URL = String.format("jdbc:mysql://%s:%s/%s", DB_HOST, DB_PORT, DB_SCHEMA);
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void afterClass() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQuery() {
        final String sql = "SELECT * FROM `user` LIMIT 10";
        try {
            ResultSet rs = statement.executeQuery(sql);
            // 展开结果集数据库
            while (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String address = rs.getString("address");
                String email = rs.getString("email");
                // 输出数据
                logger.info("id: {}, name: {}, age: {}, address: {}, email: {}", id, name, age, address, email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
