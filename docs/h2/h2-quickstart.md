# 概述

H2是一个开源的嵌入式数据库引擎，采用java语言编写，不受平台的限制。同时H2提供了一个十分方便的web控制台用于操作和管理数据库内容。H2还提供兼容模式，可以兼容一些主流的数据库，因此采用H2作为开发期的数据库非常方便。

# 使用说明

## 安装

maven中添加依赖

```xml
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <version>1.4.194</version>
</dependency>
```

## 运行方式

1. **在内存中运行**

   数据库只在内存中运行，关闭连接后数据库将被清空，适合测试环境

   连接字符串：`jdbc:h2:mem:DBName;DB_CLOSE_DELAY=-1`

   如果不指定DBName，则以私有方式启动，只允许一个连接。

2. **嵌入式**

   数据库持久化存储为单个文件。

   连接字符串：`~/.h2/DBName`表示数据库文件的存储位置，如果第一次连接则会自动创建数据库。

3. **服务模式**

   H2支持三种服务模式：

   - web server：此种运行方式支持使用浏览器访问H2 Console
   - TCP server：支持客户端/服务器端的连接方式
   - PG server：支持PostgreSQL客户端

   启动tcp服务连接字符串示例：

   ```
   jdbc:h2:tcp://localhost/~/test 使用用户主目录
   jdbc:h2:tcp://localhost//data/test 使用绝对路径
   ```

4. **连接字符串参数**

   - DB_CLOSE_DELAY：要求最后一个正在连接的连接断开后，不要关闭数据库
   - MODE=MySQL：兼容模式，H2兼容多种数据库，该值可以为：DB2、Derby、HSQLDB、MSSQLServer、MySQL、Oracle、PostgreSQL
   - AUTO_RECONNECT=TRUE：连接丢失后自动重新连接
   - AUTO_SERVER=TRUE：启动自动混合模式，允许开启多个连接，该参数不支持在内存中运行模式
   - TRACE_LEVEL_SYSTEM_OUT、TRACE_LEVEL_FILE：输出跟踪日志到控制台或文件， 取值0为OFF，1为ERROR（默认值），2为INFO，3为DEBUG
   - SET TRACE_MAX_FILE_SIZE mb：设置跟踪日志文件的大小，默认为16M

5. **启动服务模式**，打开H2 Console web页面

   启动服务，在命令行中执行

   ```shell
   java -cp h2*.jar org.h2.tools.Server
   ```

   执行如下命令，获取选项列表及默认值

   ```shell
   java -cp h2*.jar org.h2.tools.Server -?
   ```

   常见的选项如下：

   - -web：启动支持H2 Console的服务
   - -webPort <port>：服务启动端口，默认为8082
   - -browser：启动H2 Console web管理页面
   - -tcp：使用TCP server模式启动
   - -pg：使用PG server模式启动

6. **maven方式**

   此外，使用maven也可以启动H2服务。添加以下插件

   ```xml
   <plugin>
     <groupId>org.codehaus.mojo</groupId>
     <artifactId>exec-maven-plugin</artifactId>
     <executions>
       <execution>
         <goals>
           <goal>java</goal>
         </goals>
       </execution>
     </executions>
     <configuration>
       <mainClass>org.h2.tools.Server</mainClass>
       <arguments>
         <argument>-web</argument>
         <argument>-webPort</argument>
         <argument>8090</argument>
         <argument>-browser</argument>
       </arguments>
     </configuration>
   </plugin>
   ```

   在命令行中执行如下命令启动H2 Console

   ```shell
   mvn exec:java
   ```

   或者建立一个bat文件

   ```shell
   @echo off
   call mvn exec:java
   pause
   ```

   此操作相当于执行了如下命令：

   ```shell
   java -jar h2-1.3.168.jar -web -webPort 8090 -browser
   ```

# Spring整合H2

1. 添加依赖

   ```xml
   <dependency>
     <groupId>com.h2database</groupId>
     <artifactId>h2</artifactId>
     <version>1.4.194</version>
   </dependency>
   ```

2. spring配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:jdbc="http://www.springframework.org/schema/jdbc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
            http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
            http://www.springframework.org/schema/jdbc
            http://www.springframework.org/schema/jdbc/spring-jdbc.xsd">

  <!--配置数据源-->
  <bean id="dataSource" class="org.h2.jdbcx.JdbcConnectionPool"
        destroy-method="dispose">
    <constructor-arg>
      <bean class="org.h2.jdbcx.JdbcDataSource">
        <!-- 内存模式 -->
        <property name="URL" value="jdbc:h2:mem:test"/>
        <!-- 文件模式 -->
        <!-- <property name="URL" value="jdbc:h2:testRestDB" /> -->
        <property name="user" value="root"/>
        <property name="password" value="root"/>
      </bean>
    </constructor-arg>
  </bean>

  <!-- JDBC模板 -->
  <bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
    <constructor-arg ref="dataSource"/>
  </bean>
  <bean id="myJdbcTemplate" class="org.zp.notes.spring.jdbc.MyJdbcTemplate">
    <property name="jdbcTemplate" ref="jdbcTemplate"/>
  </bean>

  <!-- 初始化数据表结构 -->
  <jdbc:initialize-database data-source="dataSource" ignore-failures="ALL">
    <jdbc:script location="classpath:sql/h2/create_table_student.sql"/>
  </jdbc:initialize-database>
</beans>
```

# h2 sql语法

## SELECT

![SELECT](http://upload-images.jianshu.io/upload_images/3101171-a3f90c0d1f1f3437.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## INSERT

![INSERT](http://upload-images.jianshu.io/upload_images/3101171-6a92ae4362c3468a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## UPDATE

![UPDATE](http://upload-images.jianshu.io/upload_images/3101171-dddf0e26995d46c3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## DELETE

![DELETE](http://upload-images.jianshu.io/upload_images/3101171-96e72023445a6fd6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## BACKUP
![BACKUP](http://upload-images.jianshu.io/upload_images/3101171-6267894d24fab47f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## EXPLAIN
![EXPLAIN](http://upload-images.jianshu.io/upload_images/3101171-bbed6bb69f998b7a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

7、MERGE
![](http://upload-images.jianshu.io/upload_images/3101171-bd021648431d12a7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## RUNSCRIPT

运行sql脚本文件

![RUNSCRIPT](http://upload-images.jianshu.io/upload_images/3101171-d6fe03eff0037e14.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## SCRIPT

根据数据库创建sql脚本

![SCRIPT](http://upload-images.jianshu.io/upload_images/3101171-9ba7547ab8bcaeab.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## SHOW

![SHOW](http://upload-images.jianshu.io/upload_images/3101171-67449c6cc5cbb8c1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## ALTER

### ALTER INDEX RENAME

![ALTER INDEX RENAME](http://upload-images.jianshu.io/upload_images/3101171-230bd3f97e185d2f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### ALTER SCHEMA RENAME

![ALTER SCHEMA RENAME](http://upload-images.jianshu.io/upload_images/3101171-797a028938e46ba3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### ALTER SEQUENCE

![ALTER SEQUENCE](http://upload-images.jianshu.io/upload_images/3101171-46f343da1b6c6a29.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### ALTER TABLE

![ALTER TABLE](http://upload-images.jianshu.io/upload_images/3101171-7e146a4010f2f357.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 增加约束

![增加约束](http://upload-images.jianshu.io/upload_images/3101171-4e5605a9c87a79cb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 修改列

![修改列](http://upload-images.jianshu.io/upload_images/3101171-fbc1358c553e6614.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 删除列

![删除列](http://upload-images.jianshu.io/upload_images/3101171-dc3b897413700981.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 删除序列

![删除序列](http://upload-images.jianshu.io/upload_images/3101171-ec83899cb8724966.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### ALTER USER

#### 修改用户名

![修改用户名](http://upload-images.jianshu.io/upload_images/3101171-a1e429c0d8ece66c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 修改用户密码

![修改用户密码](http://upload-images.jianshu.io/upload_images/3101171-5b86f98796606e54.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### ALTER VIEW

![ALTER VIEW](http://upload-images.jianshu.io/upload_images/3101171-8832ecbc2db63a13.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## COMMENT

![COMMENT](http://upload-images.jianshu.io/upload_images/3101171-467ce031883f0020.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE CONSTANT
![CREATE CONSTANT](http://upload-images.jianshu.io/upload_images/3101171-1231c83563bfec9c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE INDEX
![CREATE INDEX](http://upload-images.jianshu.io/upload_images/3101171-d66d59bd7803d5c1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE ROLE
![CREATE ROLE](http://upload-images.jianshu.io/upload_images/3101171-7df1dee098e1127b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE SCHEMA
![CREATE SCHEMA](http://upload-images.jianshu.io/upload_images/3101171-c485123c62c0866e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE SEQUENCE
![CREATE SEQUENCE](http://upload-images.jianshu.io/upload_images/3101171-cc25860776d361ae.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE TABLE
![CREATE TABLE](http://upload-images.jianshu.io/upload_images/3101171-36ffc66327df8b5b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE TRIGGER
![CREATE TRIGGER](http://upload-images.jianshu.io/upload_images/3101171-9a7bfa4425281213.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE USER
![CREATE USER](http://upload-images.jianshu.io/upload_images/3101171-a1e45e308be6dac3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## CREATE VIEW
![CREATE VIEW](http://upload-images.jianshu.io/upload_images/3101171-45c4cd516fd36611.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## DROP
![DROP](http://upload-images.jianshu.io/upload_images/3101171-52a3562d76411811.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## GRANT RIGHT

给schema授权授权

![授权](http://upload-images.jianshu.io/upload_images/3101171-750e96ceff00c4ee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

给schema授权给schema授权

![给schema授权](http://upload-images.jianshu.io/upload_images/3101171-22cfd65c2ff1eea5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 复制角色的权限

![复制角色的权限](http://upload-images.jianshu.io/upload_images/3101171-6cba2f1585fd913b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## REVOKE RIGHT

### 移除授权

![移除授权](http://upload-images.jianshu.io/upload_images/3101171-3f905669cbb331b7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 移除角色具有的权限

![移除角色具有的权限](http://upload-images.jianshu.io/upload_images/3101171-af77f495222f1b30.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## ROLLBACK

### 从某个还原点（savepoint）回滚

![](http://upload-images.jianshu.io/upload_images/3101171-c71a226ac4fff913.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 回滚事务

![](http://upload-images.jianshu.io/upload_images/3101171-efb65c504c7d69c2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 创建savepoint

![](http://upload-images.jianshu.io/upload_images/3101171-feefdc236d4b211d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 数据类型

![数据类型](http://upload-images.jianshu.io/upload_images/3101171-52296dd53249cdae.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## INT Type

![INT Type](http://upload-images.jianshu.io/upload_images/3101171-fe62e3d07eb93d11.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 集群

H2支持两台服务器运行两个数据库成为集群，两个数据库互为备份，如果一个服务器失效，另一个服务器仍然可以工作。另外只有服务模式支持集群配置。

H2可以通过CreateCluster工具创建集群，示例步骤如下（在在一台服务器上模拟两个数据库组成集群）：

1. 创建目录

   创建两个服务器工作的目录


2. 启动tcp服务

   执行如下命令分别在9101、9102端口启动两个使用tcp服务模式的数据库


3. 使用CreateCluster工具创建集群

   如果两个数据库不存在，该命令将会自动创建数据库。如果一个数据库失效，可以先删除坏的数据库文件，重新启动数据库，然后重新运行CreateCluster工具


4. 连接数据库
   现在可以使用如下连接字符串连接集群数据库

5. 监控集群**运行状态**
   可以使用如下命令查看配置的集群服务器是否都在运行

6. 限制
   H2的集群并不支持针对事务的负载均衡，所以很多操作会使两个数据库产生不一致的结果

   执行如下操作时请小心：

   - 自动增长列和标识列不支持集群，当插入数据时，序列值需要手动创建不支持SET AUTOCOMMIT FALSE语句；

   - 如果需要设置成为不自动提交，可以执行方法Connection.setAutoCommit(false)

# 参考资料

[h2database官网](http://www.h2database.com/html/main.html)
