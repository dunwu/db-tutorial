# SQLite

> SQLite 是一个实现了自给自足的、无服务器的、零配置的、事务性的 SQL 数据库引擎。
> :point_right: [完整示例源码](https://github.com/dunwu/db-tutorial/tree/master/codes/javadb/javadb-sqlite)

<!-- TOC depthFrom:2 depthTo:3 -->

- [简介](#简介)
    - [优点](#优点)
    - [局限](#局限)
    - [安装](#安装)
- [语法](#语法)
    - [大小写敏感](#大小写敏感)
    - [注释](#注释)
    - [创建数据库](#创建数据库)
    - [查看数据库](#查看数据库)
    - [退出数据库](#退出数据库)
    - [附加数据库](#附加数据库)
    - [分离数据库](#分离数据库)
    - [备份数据库](#备份数据库)
    - [恢复数据库](#恢复数据库)
- [数据类型](#数据类型)
    - [SQLite 存储类](#sqlite-存储类)
    - [SQLite 亲和(Affinity)类型](#sqlite-亲和affinity类型)
    - [SQLite 亲和类型(Affinity)及类型名称](#sqlite-亲和类型affinity及类型名称)
    - [Boolean 数据类型](#boolean-数据类型)
    - [Date 与 Time 数据类型](#date-与-time-数据类型)
- [SQLite 命令](#sqlite-命令)
    - [快速开始](#快速开始)
    - [常用命令清单](#常用命令清单)
    - [实战](#实战)
- [JAVA Client](#java-client)
    - [如何指定数据库文件](#如何指定数据库文件)
    - [如何使用内存数据库](#如何使用内存数据库)
- [参考资料](#参考资料)
- [:door: 传送门](#door-传送门)

<!-- /TOC -->

## 简介

### 优点

- SQLite 是自给自足的，这意味着不需要任何外部的依赖。
- SQLite 是无服务器的、零配置的，这意味着不需要安装或管理。
- SQLite 事务是完全兼容 ACID 的，允许从多个进程或线程安全访问。
- SQLite 是非常小的，是轻量级的，完全配置时小于 400KiB，省略可选功能配置时小于 250KiB。
- SQLite 支持 SQL92（SQL2）标准的大多数查询语言的功能。
- 一个完整的 SQLite 数据库是存储在一个单一的跨平台的磁盘文件。
- SQLite 使用 ANSI-C 编写的，并提供了简单和易于使用的 API。
- SQLite 可在 UNIX（Linux, Mac OS-X, Android, iOS）和 Windows（Win32, WinCE, WinRT）中运行。

### 局限

| 特性             | 描述                                                                                                             |
| ---------------- | ---------------------------------------------------------------------------------------------------------------- |
| RIGHT OUTER JOIN | 只实现了 LEFT OUTER JOIN。                                                                                       |
| FULL OUTER JOIN  | 只实现了 LEFT OUTER JOIN。                                                                                       |
| ALTER TABLE      | 支持 RENAME TABLE 和 ALTER TABLE 的 ADD COLUMN variants 命令，不支持 DROP COLUMN、ALTER COLUMN、ADD CONSTRAINT。 |
| Trigger 支持     | 支持 FOR EACH ROW 触发器，但不支持 FOR EACH STATEMENT 触发器。                                                   |
| VIEWs            | 在 SQLite 中，视图是只读的。您不可以在视图上执行 DELETE、INSERT 或 UPDATE 语句。                                 |
| GRANT 和 REVOKE  | 可以应用的唯一的访问权限是底层操作系统的正常文件访问权限。                                                       |

### 安装

Sqlite 可在 UNIX（Linux, Mac OS-X, Android, iOS）和 Windows（Win32, WinCE, WinRT）中运行。

一般，Linux 和 Mac 上会预安装 sqlite。如果没有安装，可以在[官方下载地址](https://www.sqlite.org/download.html)下载合适安装版本，自行安装。

## 语法

> 这里不会详细列举所有 SQL 语法，仅列举 SQLite 除标准 SQL 以外的，一些自身特殊的 SQL 语法。
>
> :point_right: 扩展阅读：[标准 SQL 基本语法](https://github.com/dunwu/blog/blob/master/docs/database/sql/sql.md)

### 大小写敏感

SQLite 是**不区分大小写**的，但也有一些命令是大小写敏感的，比如 **GLOB** 和 **glob** 在 SQLite 的语句中有不同的含义。

### 注释

```sql
-- 单行注释
/*
 多行注释1
 多行注释2
 */
```

### 创建数据库

如下，创建一个名为 test 的数据库：

```bash
$ sqlite3 test.db
SQLite version 3.7.17 2013-05-20 00:56:22
Enter ".help" for instructions
Enter SQL statements terminated with a ";"
```

### 查看数据库

```bash
sqlite> .databases
seq  name             file
---  ---------------  ----------------------------------------------------------
0    main             /root/test.db
```

### 退出数据库

```
sqlite> .quit
```

### 附加数据库

假设这样一种情况，当在同一时间有多个数据库可用，您想使用其中的任何一个。

SQLite 的 **`ATTACH DATABASE`** 语句是用来选择一个特定的数据库，使用该命令后，所有的 SQLite 语句将在附加的数据库下执行。

```bash
sqlite> ATTACH DATABASE 'test.db' AS 'test';
sqlite> .databases
seq  name             file
---  ---------------  ----------------------------------------------------------
0    main             /root/test.db
2    test             /root/test.db
```

> 注意：数据库名 **`main`** 和 **`temp`** 被保留用于主数据库和存储临时表及其他临时数据对象的数据库。这两个数据库名称可用于每个数据库连接，且不应该被用于附加，否则将得到一个警告消息。

### 分离数据库

SQLite 的 **`DETACH DATABASE`** 语句是用来把命名数据库从一个数据库连接分离和游离出来，连接是之前使用 **`ATTACH`** 语句附加的。

```bash
sqlite> .databases
seq  name             file
---  ---------------  ----------------------------------------------------------
0    main             /root/test.db
2    test             /root/test.db
sqlite> DETACH DATABASE 'test';
sqlite> .databases
seq  name             file
---  ---------------  ----------------------------------------------------------
0    main             /root/test.db
```

### 备份数据库

如下，备份 test 数据库到 `/home/test.sql`

```bash
$ sqlite3 test.db .dump > /home/test.sql
```

### 恢复数据库

如下，根据 `/home/test.sql` 恢复 test 数据库

```bash
$ sqlite3 test.db < test.sql
```

## 数据类型

SQLite 使用一个更普遍的动态类型系统。在 SQLite 中，值的数据类型与值本身是相关的，而不是与它的容器相关。

### SQLite 存储类

每个存储在 SQLite 数据库中的值都具有以下存储类之一：

| 存储类    | 描述                                                                    |
| --------- | ----------------------------------------------------------------------- |
| `NULL`    | 值是一个 NULL 值。                                                      |
| `INTEGER` | 值是一个带符号的整数，根据值的大小存储在 1、2、3、4、6 或 8 字节中。    |
| `REAL`    | 值是一个浮点值，存储为 8 字节的 IEEE 浮点数字。                         |
| `TEXT`    | 值是一个文本字符串，使用数据库编码（UTF-8、UTF-16BE 或 UTF-16LE）存储。 |
| `BLOB`    | 值是一个 blob 数据，完全根据它的输入存储。                              |

SQLite 的存储类稍微比数据类型更普遍。INTEGER 存储类，例如，包含 6 种不同的不同长度的整数数据类型。

### SQLite 亲和(Affinity)类型

SQLite 支持列的亲和类型概念。任何列仍然可以存储任何类型的数据，当数据插入时，该字段的数据将会优先采用亲缘类型作为该值的存储方式。SQLite 目前的版本支持以下五种亲缘类型：

| 亲和类型  | 描述                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `TEXT`    | 数值型数据在被插入之前，需要先被转换为文本格式，之后再插入到目标字段中。                                                                                                                                                                                                                                                                                                                                                                                            |
| `NUMERIC` | 当文本数据被插入到亲缘性为 NUMERIC 的字段中时，如果转换操作不会导致数据信息丢失以及完全可逆，那么 SQLite 就会将该文本数据转换为 INTEGER 或 REAL 类型的数据，如果转换失败，SQLite 仍会以 TEXT 方式存储该数据。对于 NULL 或 BLOB 类型的新数据，SQLite 将不做任何转换，直接以 NULL 或 BLOB 的方式存储该数据。需要额外说明的是，对于浮点格式的常量文本，如"30000.0"，如果该值可以转换为 INTEGER 同时又不会丢失数值信息，那么 SQLite 就会将其转换为 INTEGER 的存储方式。 |
| `INTEGER` | 对于亲缘类型为 INTEGER 的字段，其规则等同于 NUMERIC，唯一差别是在执行 CAST 表达式时。                                                                                                                                                                                                                                                                                                                                                                               |
| `REAL`    | 其规则基本等同于 NUMERIC，唯一的差别是不会将"30000.0"这样的文本数据转换为 INTEGER 存储方式。                                                                                                                                                                                                                                                                                                                                                                        |
| `NONE`    | 不做任何的转换，直接以该数据所属的数据类型进行存储。                                                                                                                                                                                                                                                                                                                                                                                                                |

### SQLite 亲和类型(Affinity)及类型名称

下表列出了当创建 SQLite3 表时可使用的各种数据类型名称，同时也显示了相应的亲和类型：

| 数据类型                                                                                                                        | 亲和类型  |
| ------------------------------------------------------------------------------------------------------------------------------- | --------- |
| `INT`, `INTEGER`, `TINYINT`, `SMALLINT`, `MEDIUMINT`, `BIGINT`, `UNSIGNED BIG INT`, `INT2`, `INT8`                              | `INTEGER` |
| `CHARACTER(20)`, `VARCHAR(255)`, `VARYING CHARACTER(255)`, `NCHAR(55)`, `NATIVE CHARACTER(70)`, `NVARCHAR(100)`, `TEXT`, `CLOB` | `TEXT`    |
| `BLOB`, `no datatype specified`                                                                                                 | `NONE`    |
| `REAL`, `DOUBLE`, `DOUBLE PRECISION`, `FLOAT`                                                                                   | `REAL`    |
| `NUMERIC`, `DECIMAL(10,5)`, `BOOLEAN`, `DATE`, `DATETIME`                                                                       | `NUMERIC` |

### Boolean 数据类型

SQLite 没有单独的 Boolean 存储类。相反，布尔值被存储为整数 0（false）和 1（true）。

### Date 与 Time 数据类型

SQLite 没有一个单独的用于存储日期和/或时间的存储类，但 SQLite 能够把日期和时间存储为 TEXT、REAL 或 INTEGER 值。

| 存储类    | 日期格式                                                       |
| --------- | -------------------------------------------------------------- |
| `TEXT`    | 格式为 "YYYY-MM-DD HH:MM:SS.SSS" 的日期。                      |
| `REAL`    | 从公元前 4714 年 11 月 24 日格林尼治时间的正午开始算起的天数。 |
| `INTEGER` | 从 1970-01-01 00:00:00 UTC 算起的秒数。                        |

您可以以任何上述格式来存储日期和时间，并且可以使用内置的日期和时间函数来自由转换不同格式。

## SQLite 命令

### 快速开始

#### 进入 SQLite 控制台

```bash
$ sqlite3
SQLite version 3.7.17 2013-05-20 00:56:22
Enter ".help" for instructions
Enter SQL statements terminated with a ";"
sqlite>
```

#### 进入 SQLite 控制台并指定数据库

```bash
$ sqlite3 test.db
SQLite version 3.7.17 2013-05-20 00:56:22
Enter ".help" for instructions
Enter SQL statements terminated with a ";"
sqlite>
```

#### 退出 SQLite 控制台

```bash
sqlite>.quit
```

#### 查看命令帮助

```bash
sqlite>.help
```

### 常用命令清单

| 命令                  | 描述                                                                                                                                                                                                                                                      |
| --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| .backup ?DB? FILE     | 备份 DB 数据库（默认是 "main"）到 FILE 文件。                                                                                                                                                                                                             |
| .bail ON\|OFF         | 发生错误后停止。默认为 OFF。                                                                                                                                                                                                                              |
| .databases            | 列出数据库的名称及其所依附的文件。                                                                                                                                                                                                                        |
| .dump ?TABLE?         | 以 SQL 文本格式转储数据库。如果指定了 TABLE 表，则只转储匹配 LIKE 模式的 TABLE 表。                                                                                                                                                                       |
| .echo ON\|OFF         | 开启或关闭 echo 命令。                                                                                                                                                                                                                                    |
| .exit                 | 退出 SQLite 提示符。                                                                                                                                                                                                                                      |
| .explain ON\|OFF      | 开启或关闭适合于 EXPLAIN 的输出模式。如果没有带参数，则为 EXPLAIN on，及开启 EXPLAIN。                                                                                                                                                                    |
| .header(s) ON\|OFF    | 开启或关闭头部显示。                                                                                                                                                                                                                                      |
| .help                 | 显示消息。                                                                                                                                                                                                                                                |
| .import FILE TABLE    | 导入来自 FILE 文件的数据到 TABLE 表中。                                                                                                                                                                                                                   |
| .indices ?TABLE?      | 显示所有索引的名称。如果指定了 TABLE 表，则只显示匹配 LIKE 模式的 TABLE 表的索引。                                                                                                                                                                        |
| .load FILE ?ENTRY?    | 加载一个扩展库。                                                                                                                                                                                                                                          |
| .log FILE\|off        | 开启或关闭日志。FILE 文件可以是 stderr（标准错误）/stdout（标准输出）。                                                                                                                                                                                   |
| .mode MODE            | 设置输出模式，MODE 可以是下列之一：**csv** 逗号分隔的值**column** 左对齐的列**html** HTML 的 <table> 代码**insert** TABLE 表的 SQL 插入（insert）语句**line** 每行一个值**list** 由 .separator 字符串分隔的值**tabs** 由 Tab 分隔的值**tcl** TCL 列表元素 |
| .nullvalue STRING     | 在 NULL 值的地方输出 STRING 字符串。                                                                                                                                                                                                                      |
| .output FILENAME      | 发送输出到 FILENAME 文件。                                                                                                                                                                                                                                |
| .output stdout        | 发送输出到屏幕。                                                                                                                                                                                                                                          |
| .print STRING...      | 逐字地输出 STRING 字符串。                                                                                                                                                                                                                                |
| .prompt MAIN CONTINUE | 替换标准提示符。                                                                                                                                                                                                                                          |
| .quit                 | 退出 SQLite 提示符。                                                                                                                                                                                                                                      |
| .read FILENAME        | 执行 FILENAME 文件中的 SQL。                                                                                                                                                                                                                              |
| .schema ?TABLE?       | 显示 CREATE 语句。如果指定了 TABLE 表，则只显示匹配 LIKE 模式的 TABLE 表。                                                                                                                                                                                |
| .separator STRING     | 改变输出模式和 .import 所使用的分隔符。                                                                                                                                                                                                                   |
| .show                 | 显示各种设置的当前值。                                                                                                                                                                                                                                    |
| .stats ON\|OFF        | 开启或关闭统计。                                                                                                                                                                                                                                          |
| .tables ?PATTERN?     | 列出匹配 LIKE 模式的表的名称。                                                                                                                                                                                                                            |
| .timeout MS           | 尝试打开锁定的表 MS 毫秒。                                                                                                                                                                                                                                |
| .width NUM NUM        | 为 "column" 模式设置列宽度。                                                                                                                                                                                                                              |
| .timer ON\|OFF        | 开启或关闭 CPU 定时器。                                                                                                                                                                                                                                   |

### 实战

#### 格式化输出

```
sqlite>.header on
sqlite>.mode column
sqlite>.timer on
sqlite>
```

#### 输出结果到文件

```bash
sqlite> .mode list
sqlite> .separator |
sqlite> .output teyptest_file_1.txt
sqlite> select * from tbl1;
sqlite> .exit
$ cat test_file_1.txt
hello|10
goodbye|20
$
```

## JAVA Client

（1）在[官方下载地址](https://bitbucket.org/xerial/sqlite-jdbc/downloads)下载 sqlite-jdbc-(VERSION).jar ，然后将 jar 包放在项目中的 classpath。

（2）通过 API 打开一个 SQLite 数据库连接。

执行方法：

```
> javac Sample.java
> java -classpath ".;sqlite-jdbc-(VERSION).jar" Sample   # in Windows
or
> java -classpath ".:sqlite-jdbc-(VERSION).jar" Sample   # in Mac or Linux
name = leo
id = 1
name = yui
id = 2
```

示例：

```java
public class Sample {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            // 创建数据库连接
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // 设置 sql 执行超时时间为 30s

            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("create table person (id integer, name string)");
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // 读取结果集
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            // 如果错误信息是 "out of memory"，可能是找不到数据库文件
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // 关闭连接失败
                System.err.println(e.getMessage());
            }
        }
    }
}
```

### 如何指定数据库文件

Windows

```
Connection connection = DriverManager.getConnection("jdbc:sqlite:C:/work/mydatabase.db");
```

Unix (Linux, Mac OS X, etc)

```
Connection connection = DriverManager.getConnection("jdbc:sqlite:/home/leo/work/mydatabase.db");
```

### 如何使用内存数据库

```
Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
```

## 参考资料

- [SQLite 官网](https://www.sqlite.org/index.html)
- [SQLite 官方文档](https://www.sqlite.org/docs.html)
- [SQLite 官方命令行手册](https://www.sqlite.org/cli.html)
- http://www.runoob.com/sqlite/sqlite-commands.html
- https://github.com/xerial/sqlite-jdbc
- http://www.runoob.com/sqlite/sqlite-java.html

## :door: 传送门

| [我的 Github 博客](https://github.com/dunwu/blog) | [db-tutorial 首页](https://github.com/dunwu/db-tutorial) |

