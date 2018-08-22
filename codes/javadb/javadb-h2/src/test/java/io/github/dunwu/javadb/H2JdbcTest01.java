package io.github.dunwu.javadb;

import java.sql.*;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("all")
public class H2JdbcTest01 {

    // 数据库连接 URL，当前连接的是 C:\Users\Administrator 目录下的 test 数据库（连用户目录下的 test 数据库）
    private static final String JDBC_URL = "jdbc:h2:~/test";
    // 数据库连接 URL，当前连接的是 D:\Tools\h2-2018-03-18\data 目录下的 test 数据库
    private static final String JDBC_URL2 = "jdbc:h2:D:\\Tools\\h2-2018-03-18\\data\\test";
    // TCP 连接方式和其他数据库类似，是基于服务的形式进行连接，因此允许多个客户端同时连接到 H2 数据库
    private static final String JDBC_URL3 = "jdbc:h2:tcp://localhost/~/test";

    // 连接数据库时使用的用户名
    private static final String USER = "sa";
    // 连接数据库时使用的密码
    private static final String PASSWORD = "";
    // 连接H2数据库时使用的驱动类，org.h2.Driver 这个类是由 H2 数据库自己提供的，在 H2 数据库的 jar 包中可以找到
    private static final String DRIVER_CLASS = "org.h2.Driver";

    private static Connection CONNECTION = null;
    private static Statement STATEMENT = null;

    @BeforeClass
    public static void beforeClass() {
        try {
            // 加载H2数据库驱动
            Class.forName(DRIVER_CLASS);
            // 根据连接URL，用户名，密码获取数据库连接（体会下不同 URL 连接的不同之处）
            // CONNECTION = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            // CONNECTION = DriverManager.getConnection(JDBC_URL2, USER, PASSWORD);
            CONNECTION = DriverManager.getConnection(JDBC_URL3, USER, PASSWORD);
            // 创建sql声明
            STATEMENT = CONNECTION.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() {
        try {
            // 释放资源
            STATEMENT.close();
            // 关闭连接
            CONNECTION.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        try {
            // 如果存在USER_INFO表就先删除USER_INFO表
            STATEMENT.execute("DROP TABLE IF EXISTS USER_INFO");
            // 创建USER_INFO表
            STATEMENT.execute(
                    "CREATE TABLE USER_INFO(id VARCHAR(36) PRIMARY KEY,name VARCHAR(100),sex VARCHAR(4))");
            // 新增
            STATEMENT.executeUpdate(
                    "INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','带头大哥','男')");
            STATEMENT.executeUpdate(
                    "INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','萧峰','男')");
            STATEMENT.executeUpdate(
                    "INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','段誉','男')");
            STATEMENT.executeUpdate(
                    "INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','虚竹','男')");
            STATEMENT.executeUpdate(
                    "INSERT INTO USER_INFO VALUES('" + UUID.randomUUID() + "','王语嫣','女')");
            // 删除
            STATEMENT.executeUpdate("DELETE FROM USER_INFO WHERE name='带头大哥'");
            // 修改
            STATEMENT.executeUpdate("UPDATE USER_INFO SET name='大轮明王' WHERE name='鸠摩智'");
            // 查询
            ResultSet rs = STATEMENT.executeQuery("SELECT * FROM USER_INFO");
            // 遍历结果集
            while (rs.next()) {
                System.out.println(rs.getString("id") + "," + rs.getString("name") + ","
                        + rs.getString("sex"));
            }
        } catch (SQLException e) {
            Assert.assertTrue(e.getMessage(), true);
        }
    }
}
