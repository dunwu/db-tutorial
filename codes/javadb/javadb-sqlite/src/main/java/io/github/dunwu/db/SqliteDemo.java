package io.github.dunwu.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author Zhang Peng
 * @date 2019-03-05
 */
public class SqliteDemo {

    public static void createTable() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");

            Statement statement = connection.createStatement();
            String sql = new StringBuilder().append("CREATE TABLE COMPANY ").append("(ID INT PRIMARY KEY     NOT NULL,")
                                            .append(" NAME           TEXT    NOT NULL, ")
                                            .append(" AGE            INT     NOT NULL, ")
                                            .append(" ADDRESS        CHAR(50), ").append(" SALARY         REAL)")
                                            .toString();
            statement.executeUpdate(sql);
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Create table successfully.");
    }

    public static void dropTable() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");

            Statement statement = connection.createStatement();
            String sql = new StringBuilder().append("DROP TABLE IF EXISTS COMPANY;").toString();
            statement.executeUpdate(sql);
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Drop table successfully.");
    }

    public static void insert() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();
            String sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
                + "VALUES (1, 'Paul', 32, 'California', 20000.00 );";
            statement.executeUpdate(sql);

            sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " + "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
            statement.executeUpdate(sql);

            sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " + "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
            statement.executeUpdate(sql);

            sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
                + "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
            statement.executeUpdate(sql);

            statement.close();
            connection.commit();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Insert table successfully.");
    }

    public static void delete() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();
            String sql = "DELETE from COMPANY where ID=2;";
            statement.executeUpdate(sql);

            String sql2 = "DELETE from COMPANY where ID=3;";
            statement.executeUpdate(sql2);

            String sql3 = "DELETE from COMPANY where ID=4;";
            statement.executeUpdate(sql3);
            connection.commit();

            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Delete table successfully.");
    }

    public static void update() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();
            String sql = "UPDATE COMPANY set SALARY = 25000.00 where ID=1;";
            statement.executeUpdate(sql);
            connection.commit();

            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Update table successfully.");
    }

    public static void select() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            connection.setAutoCommit(false);

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM COMPANY;");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String address = resultSet.getString("address");
                float salary = resultSet.getFloat("salary");
                String format = String
                    .format("ID = %s, NAME = %s, AGE = %d, ADDRESS = %s, SALARY = %f", id, name, age, address, salary);
                System.out.println(format);
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SqliteDemo.dropTable();
        SqliteDemo.createTable();
        SqliteDemo.insert();
        SqliteDemo.select();
        SqliteDemo.delete();
        SqliteDemo.select();
        SqliteDemo.update();
        SqliteDemo.select();
    }
}
