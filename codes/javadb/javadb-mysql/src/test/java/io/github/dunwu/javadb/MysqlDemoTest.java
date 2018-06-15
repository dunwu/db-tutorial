package io.github.dunwu.javadb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mysql 测试例
 * @author Zhang Peng
 * @see https://dev.mysql.com/doc/connector-j/5.1/en/
 */
public class MysqlDemoTest {

    private static Logger logger = LoggerFactory.getLogger(MysqlDemoTest.class);

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_SCHEMA = "sakila";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    private static Statement statement;
    private static Connection connection;

    @BeforeClass
    public static void beforeClass() {
        try {
            final String DB_URL = String.format("jdbc:mysql://%s:%s/%s", DB_HOST, DB_PORT, DB_SCHEMA);
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila?" +
            // "user=root&password=root");
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
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
    public void testString() {
        final String sql = "select * from actor limit 10";
        try {
            ResultSet rs = statement.executeQuery(sql);
            // 展开结果集数据库
            while (rs.next()) {
                // 通过字段检索
                int id = rs.getInt("actor_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                Date lastUpdate = rs.getDate("last_update");
                // 输出数据
                logger.debug("actor_id: {}, first_name: {}, last_name: {}, last_update: {}", id, firstName, lastName,
                    lastUpdate.toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
