# Flyway

> Flyway 是一个数据迁移工具。
>
> 关键词：

<!-- TOC depthFrom:2 depthTo:3 -->

- [简介](#简介)
    - [什么是 Flyway？](#什么是-flyway)
    - [为什么要使用数据迁移？](#为什么要使用数据迁移)
    - [Flyway 如何工作？](#flyway-如何工作)
- [快速上手](#快速上手)
    - [命令行](#命令行)
    - [JAVA API](#java-api)
    - [Maven](#maven)
    - [Gradle](#gradle)
- [入门篇](#入门篇)
    - [概念](#概念)
    - [命令](#命令)
    - [支持的数据库](#支持的数据库)
- [资料](#资料)
- [:door: 传送门](#door-传送门)

<!-- /TOC -->

## 简介

### 什么是 Flyway？

**Flyway 是一个开源的数据库迁移工具。**

### 为什么要使用数据迁移？

为了说明数据迁移的作用，我们来举一个示例：

（1）假设，有一个叫做 Shiny 的项目，它的架构是一个叫做 Shiny Soft 的 App 连接叫做 Shiny DB 的数据库。

（2）对于大多数项目而言，最简单的持续集成场景如下所示：

<br><div align="center"><img src="https://flywaydb.org/assets/balsamiq/Environments.png"/></div><br>

这意味着，我们不仅仅要处理一份环境中的修改，由此会引入一些版本冲突问题：

在代码侧（即应用软件）的版本问题比较容易解决：

- 有方便的版本控制工具
- 有可复用的构建和持续集成
- 规范的发布和部署过程

那么，数据库层面的版本问题如何解决呢？

目前仍然没有方便的数据库版本工具。许多项目仍使用 sql 脚本来解决版本冲突，甚至是遇到冲突问题时才想起用 sql 语句去解决。

由此，引发一些问题：

- 机器上的数据库是什么状态？
- 脚本到底生效没有？
- 生产环境修复的问题是否也在测试环境修复了？
- 如何建立一个新的数据库实例？

数据迁移就是用来搞定这些混乱的问题：

- 通过草稿重建一个数据库。
- 在任何时候都可以清楚的了解数据库的状态。
- 以一种明确的方式将数据库从当前版本迁移到一个新版本。

### Flyway 如何工作？

最简单的场景是指定 Flyway 迁移到一个空的数据库。

<br><div align="center"><img src="http://upload-images.jianshu.io/upload_images/3101171-bb6e9f39e56ebbda.png"/></div><br>

Flyway 会尝试查找它的 schema 历史表，如果数据库是空的，Flyway 就不再查找，而是直接创建数据库。

现再你就有了一个仅包含一张空表的数据库，默认情况下，这张表叫 _flyway_schema_history_。

<br><div align="center"><img src="http://upload-images.jianshu.io/upload_images/3101171-410eb31c6313b389.png"/></div><br>

这张表将被用于追踪数据库的状态。

然后，Flyway 将开始扫描文件系统或应用 classpath 中的 **migrations**。这些 **migrations** 可以是 sql 或 java。

这些 **migrations** 将根据他们的版本号进行排序。

<br><div align="center"><img src="http://upload-images.jianshu.io/upload_images/3101171-d36ee07ada4efbcd.png"/></div><br>

任意 migration 应用后，schema 历史表将更新。当元数据和初始状态替换后，可以称之为：迁移到新版本。

Flyway 一旦扫描了文件系统或应用 classpath 下的 migrations，这些 migrations 会检查 schema 历史表。如果它们的版本号低于或等于当前的版本，将被忽略。保留下来的 migrations 是等待的 migrations，有效但没有应用。

<br><div align="center"><img src="http://upload-images.jianshu.io/upload_images/3101171-99a88fea7a31a070.png"/></div><br>

migrations 将根据版本号排序并按序执行。

<br><div align="center"><img src="http://upload-images.jianshu.io/upload_images/3101171-b444fef6e5c13b71.png"/></div><br>

## 快速上手

Flyway 有 4 种使用方式：

- 命令行
- JAVA API
- Maven
- Gradle

### 命令行

适用于非 Java 用户，无需构建。

```shell
> flyway migrate -url=... -user=... -password=...
```

（1）**下载解压**

进入[官方下载页面](https://flywaydb.org/download/)，选择合适版本，下载并解压到本地。

（2）**配置 flyway**

编辑 `/conf/flyway.conf`：

```properties
flyway.url=jdbc:h2:file:./foobardb
flyway.user=SA
flyway.password=
```

（3）**创建第一个 migration**

在 `/sql` 目录下创建 `V1__Create_person_table.sql` 文件，内容如下：

```sql
create table PERSON (
    ID int not null,
    NAME varchar(100) not null
);
```

（4）**迁移数据库**

运行 Flyway 来迁移数据库：

```bash
flyway-5.1.4> flyway migrate
```

运行正常的情况下，应该可以看到如下结果：

```
Database: jdbc:h2:file:./foobardb (H2 1.4)
Successfully validated 1 migration (execution time 00:00.008s)
Creating Schema History table: "PUBLIC"."flyway_schema_history"
Current version of schema "PUBLIC": << Empty Schema >>
Migrating schema "PUBLIC" to version 1 - Create person table
Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.033s)
```

（5）**添加第二个 migration**

在 `/sql` 目录下创建 `V2__Add_people.sql` 文件，内容如下：

```sql
insert into PERSON (ID, NAME) values (1, 'Axel');
insert into PERSON (ID, NAME) values (2, 'Mr. Foo');
insert into PERSON (ID, NAME) values (3, 'Ms. Bar');
```

运行 Flyway

```bash
flyway-5.1.4> flyway migrate
```

运行正常的情况下，应该可以看到如下结果：

```
Database: jdbc:h2:file:./foobardb (H2 1.4)
Successfully validated 2 migrations (execution time 00:00.018s)
Current version of schema "PUBLIC": 1
Migrating schema "PUBLIC" to version 2 - Add people
Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.016s)
```

### JAVA API

（1）**准备**

- Java8+
- Maven 3.x

（2）**添加依赖**

在 `pom.xml` 中添加依赖：

```xml
<project ...>
    ...
    <dependencies>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
            <version>5.1.4</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>1.3.170</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
```

（3）**集成 Flyway**

添加 `App.java` 文件，内容如下：

```java
import org.flywaydb.core.Flyway;

public class App {
    public static void main(String[] args) {
        // Create the Flyway instance
        Flyway flyway = new Flyway();

        // Point it to the database
        flyway.setDataSource("jdbc:h2:file:./target/foobar", "sa", null);

        // Start the migration
        flyway.migrate();
    }
}
```

（4）**创建第一个 migration**

添加 `src/main/resources/db/migration/V1__Create_person_table.sql` 文件，内容如下：

```sql
create table PERSON (
    ID int not null,
    NAME varchar(100) not null
);
```

（5）**执行程序**

执行 `App#main`：

运行正常的情况下，应该可以看到如下结果：

```
INFO: Creating schema history table: "PUBLIC"."flyway_schema_history"
INFO: Current version of schema "PUBLIC": << Empty Schema >>
INFO: Migrating schema "PUBLIC" to version 1 - Create person table
INFO: Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.062s).
```

（6）**添加第二个 migration**

添加 src/main/resources/db/migration/V2\_\_Add_people.sql 文件，内容如下：

```sql
insert into PERSON (ID, NAME) values (1, 'Axel');
insert into PERSON (ID, NAME) values (2, 'Mr. Foo');
insert into PERSON (ID, NAME) values (3, 'Ms. Bar');
```

运行正常的情况下，应该可以看到如下结果：

```
INFO: Current version of schema "PUBLIC": 1
INFO: Migrating schema "PUBLIC" to version 2 - Add people
INFO: Successfully applied 1 migration to schema "PUBLIC" (execution time 00:00.090s).
```

### Maven

与 Java API 方式大体相同，区别在 **集成 Flyway** 步骤：

Maven 方式使用插件来集成 Flyway：

```xml
<project xmlns="...">
    ...
    <build>
        <plugins>
            <plugin>
                <groupId>org.flywaydb</groupId>
                <artifactId>flyway-maven-plugin</artifactId>
                <version>5.1.4</version>
                <configuration>
                    <url>jdbc:h2:file:./target/foobar</url>
                    <user>sa</user>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>com.h2database</groupId>
                        <artifactId>h2</artifactId>
                        <version>1.4.191</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```

因为用的是插件，所以执行方式不再是运行 Java 类，而是执行 maven 插件：

```bash
> mvn flyway:migrate
```

> :point_right: 参考：[示例源码](https://github.com/dunwu/Database/tree/master/codes/middleware/flyway)

### Gradle

本人不用 Gradle，略。

## 入门篇

### 概念

#### Migrations

在 Flyway 中，对于数据库的任何改变都称之为 **Migrations**。

Migrations 可以分为 Versioned migrations 和 Repeatable migrations。

Versioned migrations 有 2 种形式：regular 和 undo。

Versioned migrations 和 Repeatable migrations 都可以使用 SQL 或 JAVA 来编写。

##### Versioned migrations

由一个版本号（version）、一段描述（description）、一个校验（checksum）组成。版本号必须是惟一的。Versioned migrations 只能按顺序执行一次。

一般用于：

- 增删改 tables/indexes/foreign keys/enums/UDTs。
- 引用数据更新
- 用户数据校正

Regular 示例：

```sql
CREATE TABLE car (
    id INT NOT NULL PRIMARY KEY,
    license_plate VARCHAR NOT NULL,
    color VARCHAR NOT NULL
);

ALTER TABLE owner ADD driver_license_id VARCHAR;

INSERT INTO brand (name) VALUES ('DeLorean');
```

##### Undo migrations

> 注：仅专业版支持

Undo Versioned Migrations 负责撤销 Regular Versioned migrations 的影响。

Undo 示例：

```sql
DELETE FROM brand WHERE name='DeLorean';

ALTER TABLE owner DROP driver_license_id;

DROP TABLE car;
```

##### Repeatable migrations

由一段描述（description）、一个校验（checksum）组成。Versioned migrations 每次执行后，校验（checksum）会更新。

Repeatable migrations 用于管理可以通过一个文件来维护版本控制的数据库对象。

一般用于：

- 创建（重建）views/procedures/functions/packages 等。
- 大量引用数据重新插入

示例：

```sql
CREATE OR REPLACE VIEW blue_cars AS
    SELECT id, license_plate FROM cars WHERE color='blue';
```

##### 基于 SQL 的 migrations

migrations 最常用的编写形式就是 SQL。

基于 SQL 的 migrations 一般用于：

- DDL 变更（针对 TABLES,VIEWS,TRIGGERS,SEQUENCES 等的 CREATE/ALTER/DROP 操作）
- 简单的引用数据变更（引用数据表中的 CRUD）
- 简单的大量数据变更（常规数据表中的 CRUD）

**命名规则**

为了被 Flyway 自动识别，SQL migrations 的文件命名必须遵循规定的模式：

<br><div align="center"><img src="http://dunwu.test.upcdn.net/cs/database/flyway/sql-migrations.png!zp"/></div><br>

- **Prefix** - `V` 代表 versioned migrations (可配置), `U` 代表 undo migrations (可配置)、 `R` 代表 repeatable migrations (可配置)
- **Version** - 版本号通过`.`(点)或`_`(下划线)分隔 (repeatable migrations 不需要)
- **Separator** - `__` (两个下划线) (可配置)
- **Description** - 下划线或空格分隔的单词
- **Suffix** - `.sql` (可配置)

##### 基于 JAVA 的 migrations

基于 JAVA 的 migrations 适用于使用 SQL 不容易表达的场景：

- BLOB 和 CLOB 变更
- 大量数据的高级变更（重新计算、高级格式变更）

**命名规则**

为了被 Flyway 自动识别，JAVA migrations 的文件命名必须遵循规定的模式：

<br><div align="center"><img src="http://dunwu.test.upcdn.net/cs/database/flyway/java-migrations.png!zp"/></div><br>

- **Prefix** - `V` 代表 versioned migrations (可配置), `U` 代表 undo migrations (可配置)、 `R` 代表 repeatable migrations (可配置)
- **Version** - 版本号通过`.`(点)或`_`(下划线)分隔 (repeatable migrations 不需要)
- **Separator** - `__` (两个下划线) (可配置)
- **Description** - 下划线或空格分隔的单词

> :point_right: 更多细节请参考：https://flywaydb.org/documentation/migrations

#### Callbacks

> 注：部分 events 仅专业版支持。

尽管 Migrations 可能已经满足绝大部分场景的需要，但是某些情况下需要你一遍又一遍的执行相同的行为。这可能会重新编译存储过程，更新视图以及许多其他类型的开销。

因为以上原因，Flyway 提供了 Callbacks，用于在 Migrations 生命周期中添加钩子。

Callbacks 可以用 SQL 或 JAVA 来实现。

##### SQL Callbacks

SQL Callbacks 的命名规则为：event 名 + SQL migration。

如： `beforeMigrate.sql`, `beforeEachMigrate.sql`, `afterEachMigrate.sql` 等。

SQL Callbacks 也可以包含描述（description）。这种情况下，SQL Callbacks 文件名 = event 名 + 分隔符 + 描述 + 后缀。例：`beforeRepair__vacuum.sql`

当同一个 event 有多个 SQL callbacks，将按照它们描述（description）的顺序执行。

> **注：** Flyway 也支持你配置的 `sqlMigrationSuffixes`。

##### JAVA Callbacks

> 当 SQL Callbacks 不够方便时，才应考虑 JAVA Callbacks。

JAVA Callbacks 有 3 种形式：

1. **基于 Java 的 Migrations** - 实现 JdbcMigration、SpringJdbcMigration、MigrationInfoProvider、MigrationChecksumProvider、ConfigurationAware、FlywayConfiguration
2. **基于 Java 的 Callbacks** - 实现 org.flywaydb.core.api.callback 接口。
3. **自定义 Migration resolvers 和 executors** - 实现 MigrationResolver、MigrationExecutor、ConfigurationAware、FlywayConfiguration 接口。

> :point_right: 更多细节请参考：https://flywaydb.org/documentation/callbacks

#### Error Handlers

> 注：仅专业版支持。

（略）

#### Dry Runs

> 注：仅专业版支持。

（略）

### 命令

Flyway 的功能主要围绕着 7 个基本命令：[Migrate](https://flywaydb.org/documentation/command/migrate)、[Clean](https://flywaydb.org/documentation/command/clean)、[Info](https://flywaydb.org/documentation/command/info)、[Validate](https://flywaydb.org/documentation/command/validate)、[Undo](https://flywaydb.org/documentation/command/undo)、[Baseline](https://flywaydb.org/documentation/command/baseline) 和 [Repair](https://flywaydb.org/documentation/command/repair)。

注：各命令的使用方法细节请查阅官方文档。

### 支持的数据库

- [Oracle](https://flywaydb.org/documentation/database/oracle)
- [SQL Server](https://flywaydb.org/documentation/database/sqlserver)
- [DB2](https://flywaydb.org/documentation/database/db2)
- [MySQL](https://flywaydb.org/documentation/database/mysql)
- [MariaDB](https://flywaydb.org/documentation/database/mariadb)
- [PostgreSQL](https://flywaydb.org/documentation/database/postgresql)
- [Redshift](https://flywaydb.org/documentation/database/redshift)
- [CockroachDB](https://flywaydb.org/documentation/database/cockroachdb)
- [SAP HANA](https://flywaydb.org/documentation/database/saphana)
- [Sybase ASE](https://flywaydb.org/documentation/database/sybasease)
- [Informix](https://flywaydb.org/documentation/database/informix)
- [H2](https://flywaydb.org/documentation/database/h2)
- [HSQLDB](https://flywaydb.org/documentation/database/hsqldb)
- [Derby](https://flywaydb.org/documentation/database/derby)
- [SQLite](https://flywaydb.org/documentation/database/sqlite)

## 资料

| [Github](https://github.com/flyway/flyway) | [官方文档](https://flywaydb.org/) |

## :door: 传送门

| [我的 Github 博客](https://github.com/dunwu/blog) | [db-tutorial 首页](https://github.com/dunwu/db-tutorial) |
