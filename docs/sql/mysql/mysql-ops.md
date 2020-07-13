# Mysql 运维

> 如果你的公司有 DBA，那么我恭喜你，你可以无视 Mysql 运维。如果你的公司没有 DBA，那你就好好学两手 Mysql 基本运维操作，行走江湖，防身必备。
>
> 环境：CentOS7
>
> 版本：![mysql](https://img.shields.io/badge/mysql-8.0-blue)

<!-- TOC depthFrom:2 depthTo:3 -->

- [一、虚拟机部署](#一虚拟机部署)
  - [安装 mysql yum 源](#安装-mysql-yum-源)
  - [mysql 服务管理](#mysql-服务管理)
  - [初始化数据库密码](#初始化数据库密码)
  - [配置远程访问](#配置远程访问)
  - [跳过登录认证](#跳过登录认证)
- [二、基本运维](#二基本运维)
  - [创建用户](#创建用户)
  - [查看用户](#查看用户)
  - [授权](#授权)
  - [撤销授权](#撤销授权)
  - [查看授权](#查看授权)
  - [更改用户密码](#更改用户密码)
  - [备份与恢复](#备份与恢复)
  - [卸载](#卸载)
  - [主从节点部署](#主从节点部署)
- [三、配置](#三配置)
  - [配置文件路径](#配置文件路径)
  - [配置项语法](#配置项语法)
  - [常用配置项说明](#常用配置项说明)
- [四、常见问题](#四常见问题)
  - [Too many connections](#too-many-connections)
  - [时区（time_zone）偏差](#时区time_zone偏差)
  - [数据表损坏如何修复](#数据表损坏如何修复)
  - [数据结构](#数据结构)
- [五、脚本](#五脚本)
- [参考资料](#参考资料)

<!-- /TOC -->

## 一、虚拟机部署

> 本文仅介绍 rpm 安装方式

### 安装 mysql yum 源

官方下载地址：https://dev.mysql.com/downloads/repo/yum/

（1）下载 yum 源

```shell
wget https://dev.mysql.com/get/mysql80-community-release-el7-1.noarch.rpm
```

（2）安装 yum repo 文件并更新 yum 缓存

```shell
rpm -ivh mysql80-community-release-el7-1.noarch.rpm
```

执行结果：

会在 /etc/yum.repos.d/ 目录下生成两个 repo 文件

```shell
$ ls | grep mysql
mysql-community.repo
mysql-community-source.repo
```

更新 yum：

```shell
yum clean all
yum makecache
```

（3）查看 rpm 安装状态

```shell
$ yum search mysql | grep server
mysql-community-common.i686 : MySQL database common files for server and client
mysql-community-common.x86_64 : MySQL database common files for server and
mysql-community-test.x86_64 : Test suite for the MySQL database server
                       : administering MySQL servers
mysql-community-server.x86_64 : A very fast and reliable SQL database server
```

通过 yum 安装 mysql 有几个重要目录：

```
# 配置文件
/etc/my.cnf
# 数据库目录
/var/lib/mysql/
# 配置文件
/usr/share/mysql（mysql.server命令及配置文件）
# 相关命令
/usr/bin（mysqladmin mysqldump等命令）
# 启动脚本
/usr/lib/systemd/system/mysqld.service （注册为 systemd 服务）
```

（4）安装 mysql 服务器

```shell
yum install mysql-community-server
```

### mysql 服务管理

通过 yum 方式安装 mysql 后，本地会有一个名为 `mysqld` 的 systemd 服务。

其服务管理十分简便：

```shell
# 查看状态
systemctl status mysqld
# 启用服务
systemctl enable mysqld
# 禁用服务
systemctl disable mysqld
# 启动服务
systemctl start mysqld
# 重启服务
systemctl restart mysqld
# 停止服务
systemctl stop mysqld
```

### 初始化数据库密码

查看一下初始密码

```shell
$ grep "password" /var/log/mysqld.log
2018-09-30T03:13:41.727736Z 5 [Note] [MY-010454] [Server] A temporary password is generated for root@localhost: %:lt+srWu4k1
```

执行命令：

```shell
mysql -uroot -p<临时密码>
```

输入临时密码，进入 mysql，如果要修改密码，执行以下指令：

```shell
ALTER user 'root'@'localhost' IDENTIFIED BY '你的密码';
```

注：密码强度默认为中等，大小写字母、数字、特殊符号，只有修改成功后才能修改配置再设置更简单的密码

### 配置远程访问

```sql
CREATE USER 'root'@'%' IDENTIFIED BY '你的密码';
GRANT ALL ON *.* TO 'root'@'%';
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '你的密码';
FLUSH PRIVILEGES;
```

### 跳过登录认证

```shell
vim /etc/my.cnf
```

在 [mysqld] 下面加上 skip-grant-tables

作用是登录时跳过登录认证，换句话说就是 root 什么密码都可以登录进去。

执行 `systemctl restart mysqld`，重启 mysql

## 二、基本运维

### 创建用户

```sql
CREATE USER 'username'@'host' IDENTIFIED BY 'password';
```

说明：

- username：你将创建的用户名
- host：指定该用户在哪个主机上可以登陆，如果是本地用户可用 localhost，如果想让该用户可以**从任意远程主机登陆**，可以使用通配符`%`
- password：该用户的登陆密码，密码可以为空，如果为空则该用户可以不需要密码登陆服务器

示例：

```sql
CREATE USER 'dog'@'localhost' IDENTIFIED BY '123456';
CREATE USER 'pig'@'192.168.1.101_' IDENDIFIED BY '123456';
CREATE USER 'pig'@'%' IDENTIFIED BY '123456';
CREATE USER 'pig'@'%' IDENTIFIED BY '';
CREATE USER 'pig'@'%';
```

> 注意：在 Mysql 8 中，默认密码验证不再是 `password`。所以在创建用户时，`create user 'username'@'%' identified by 'password';` 客户端是无法连接服务的。
>
> 所以，需要加上 `IDENTIFIED WITH mysql_native_password`，例如：`CREATE USER 'slave'@'%' IDENTIFIED WITH mysql_native_password BY '123456';`

### 查看用户

```sql
-- 查看所有用户
SELECT DISTINCT CONCAT('User: ''', user, '''@''', host, ''';') AS query
FROM mysql.user;
```

### 授权

命令：

```sql
GRANT privileges ON databasename.tablename TO 'username'@'host'
```

说明：

- privileges：用户的操作权限，如`SELECT`，`INSERT`，`UPDATE`等，如果要授予所的权限则使用`ALL`
- databasename：数据库名
- tablename：表名，如果要授予该用户对所有数据库和表的相应操作权限则可用`*`表示，如`*.*`

示例：

```sql
GRANT SELECT, INSERT ON test.user TO 'pig'@'%';
GRANT ALL ON *.* TO 'pig'@'%';
GRANT ALL ON maindataplus.* TO 'pig'@'%';
```

注意：

用以上命令授权的用户不能给其它用户授权，如果想让该用户可以授权，用以下命令:

```sql
-- 为指定用户配置指定权限
GRANT privileges ON databasename.tablename TO 'username'@'host' WITH GRANT OPTION;
-- 为 root 用户分配所有权限
GRANT ALL ON *.* TO 'root'@'%' IDENTIFIED BY '密码' WITH GRANT OPTION;
```

### 撤销授权

命令:

```
REVOKE privilege ON databasename.tablename FROM 'username'@'host';
```

说明:

privilege, databasename, tablename：同授权部分

例子:

```sql
REVOKE SELECT ON *.* FROM 'pig'@'%';
```

注意:

假如你在给用户`'pig'@'%'`授权的时候是这样的（或类似的）：`GRANT SELECT ON test.user TO 'pig'@'%'`，则在使用`REVOKE SELECT ON *.* FROM 'pig'@'%';`命令并不能撤销该用户对 test 数据库中 user 表的`SELECT` 操作。相反，如果授权使用的是`GRANT SELECT ON *.* TO 'pig'@'%';`则`REVOKE SELECT ON test.user FROM 'pig'@'%';`命令也不能撤销该用户对 test 数据库中 user 表的`Select`权限。

具体信息可以用命令`SHOW GRANTS FOR 'pig'@'%';` 查看。

### 查看授权

```SQL
-- 查看用户权限
SHOW GRANTS FOR 'root'@'%';
```

### 更改用户密码

```sql
SET PASSWORD FOR 'username'@'host' = PASSWORD('newpassword');
```

如果是当前登陆用户用:

```sql
SET PASSWORD = PASSWORD("newpassword");
```

示例：

```sql
SET PASSWORD FOR 'pig'@'%' = PASSWORD("123456");
```

### 备份与恢复

Mysql 备份数据使用 mysqldump 命令。

mysqldump 将数据库中的数据备份成一个文本文件，表的结构和表中的数据将存储在生成的文本文件中。

备份：

#### 备份一个数据库

语法：

```sql
mysqldump -h <host> -P<port> -u<username> -p<database> [<table1> <table2> ...] > backup.sql
```

- **`host`** - Mysql Server 的 host
- **`port`** - Mysql Server 的端口
- **`username`** - 数据库用户
- **`dbname`** - 数据库名称
- table1 和 table2 参数表示需要备份的表的名称，为空则整个数据库备份；
- BackupName.sql 参数表设计备份文件的名称，文件名前面可以加上一个绝对路径。通常将数据库被分成一个后缀名为 sql 的文件

#### 备份多个数据库

```sql
mysqldump -u <username> -p --databases <database1> <database2> ... > backup.sql
```

#### 备份所有数据库

```sql
mysqldump -u <username> -p --all-databases > backup.sql
```

#### 恢复一个数据库

Mysql 恢复数据使用 mysql 命令。

语法：

```sql
mysql -h <host> -P<port> -u<username> -p<database> < backup.sql
```

#### 恢复所有数据库

```sql
mysql -u<username> -p --all-databases < backup.sql
```

### 卸载

（1）查看已安装的 mysql

```shell
$ rpm -qa | grep -i mysql
perl-DBD-MySQL-4.023-6.el7.x86_64
mysql80-community-release-el7-1.noarch
mysql-community-common-8.0.12-1.el7.x86_64
mysql-community-client-8.0.12-1.el7.x86_64
mysql-community-libs-compat-8.0.12-1.el7.x86_64
mysql-community-libs-8.0.12-1.el7.x86_64
```

（2）卸载 mysql

```shell
yum remove mysql-community-server.x86_64
```

### 主从节点部署

假设需要配置一个主从 Mysql 服务器环境

- master 节点：192.168.8.10
- slave 节点：192.168.8.11

#### 主节点上的操作

（1）修改配置并重启

执行 `vi /etc/my.cnf` ，添加如下配置：

```ini
[mysqld]
server-id=1
log_bin=/var/lib/mysql/binlog
```

- `server-id` - 服务器 ID 号。在主从架构中，每台机器的 ID 必须唯一。
- `log_bin` - 同步的日志路径及文件名，一定注意这个目录要是 mysql 有权限写入的；

修改后，重启 mysql 使配置生效：

```sql
systemctl restart mysql
```

（2）创建用于同步的用户

进入 mysql 命令控制台：

```
$ mysql -u root -p
Password:
```

执行以下 SQL：

```sql
-- a. 创建 slave 用户
CREATE USER 'slave'@'%' IDENTIFIED WITH mysql_native_password BY '密码';
-- 为 slave 赋予 REPLICATION SLAVE 权限
GRANT REPLICATION SLAVE ON *.* TO 'slave'@'%';

-- b. 或者，创建 slave 用户，并指定该用户能在任意主机上登录
-- 如果有多个从节点，又想让所有从节点都使用统一的用户名、密码认证，可以考虑这种方式
CREATE USER 'slave'@'%' IDENTIFIED WITH mysql_native_password BY '密码';
GRANT REPLICATION SLAVE ON *.* TO 'slave'@'%';

-- 刷新授权表信息
FLUSH PRIVILEGES;
```

> 注意：在 Mysql 8 中，默认密码验证不再是 `password`。所以在创建用户时，`create user 'username'@'%' identified by 'password';` 客户端是无法连接服务的。所以，需要加上 `IDENTIFIED WITH mysql_native_password BY 'password'`

补充用户管理 SQL:

```sql
-- 查看所有用户
SELECT DISTINCT CONCAT('User: ''', user, '''@''', host, ''';') AS query
FROM mysql.user;

-- 查看用户权限
SHOW GRANTS FOR 'root'@'%';

-- 创建用户
-- a. 创建 slave 用户，并指定该用户只能在主机 192.168.8.11 上登录
CREATE USER 'slave'@'192.168.8.11' IDENTIFIED WITH mysql_native_password BY '密码';
-- 为 slave 赋予 REPLICATION SLAVE 权限
GRANT REPLICATION SLAVE ON *.* TO 'slave'@'192.168.8.11';

-- 删除用户
DROP USER 'slave'@'192.168.8.11';
```

（3）加读锁

为了主库与从库的数据保持一致，我们先为 mysql 加入读锁，使其变为只读。

```sql
mysql> FLUSH TABLES WITH READ LOCK;
```

（4）查看主节点状态

```sql
mysql> show master status;
+------------------+----------+--------------+---------------------------------------------+-------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB                            | Executed_Gtid_Set |
+------------------+----------+--------------+---------------------------------------------+-------------------+
| mysql-bin.000001 |     4202 |              | mysql,information_schema,performance_schema |                   |
+------------------+----------+--------------+---------------------------------------------+-------------------+
1 row in set (0.00 sec)
```

> 注意：需要记录下 `File` 和 `Position`，后面会用到。

（5）导出 sql

```shell
mysqldump -u root -p --all-databases --master-data > dbdump.sql
```

（6）解除读锁

```sql
mysql> UNLOCK TABLES;
```

（7）将 sql 远程传送到从节点上

```
scp dbdump.sql root@192.168.8.11:/home
```

#### 从节点上的操作

（1）修改配置并重启

执行 `vi /etc/my.cnf` ，添加如下配置：

```ini
[mysqld]
server-id=2
log_bin=/var/lib/mysql/binlog
```

- `server-id` - 服务器 ID 号。在主从架构中，每台机器的 ID 必须唯一。
- `log_bin` - 同步的日志路径及文件名，一定注意这个目录要是 mysql 有权限写入的；

修改后，重启 mysql 使配置生效：

```shell
systemctl restart mysql
```

（2）导入 sql

```shell
mysql -u root -p < /home/dbdump.sql
```

（3）在从节点上建立与主节点的连接

进入 mysql 命令控制台：

```
$ mysql -u root -p
Password:
```

执行以下 SQL：

```sql
-- 停止从节点服务
STOP SLAVE;

-- 注意：MASTER_USER 和
CHANGE MASTER TO
MASTER_HOST='192.168.8.10',
MASTER_USER='slave',
MASTER_PASSWORD='密码',
MASTER_LOG_FILE='binlog.000001',
MASTER_LOG_POS=4202;
```

- `MASTER_LOG_FILE` 和 `MASTER_LOG_POS` 参数要分别与 `show master status` 指令获得的 `File` 和 `Position` 属性值对应。
- `MASTER_HOST` 是主节点的 HOST。
- `MASTER_USER` 和 `MASTER_PASSWORD` 是在主节点上注册的用户及密码。

（4）启动 slave 进程

```sql
mysql> start slave;
```

（5）查看主从同步状态

```sql
mysql> show slave status\G;
```

说明：如果以下两项参数均为 YES，说明配置正确。

- `Slave_IO_Running`
- `Slave_SQL_Running`

（6）将从节点设为只读

```sql
mysql> set global read_only=1;
mysql> set global super_read_only=1;
mysql> show global variables like "%read_only%";
+-----------------------+-------+
| Variable_name         | Value |
+-----------------------+-------+
| innodb_read_only      | OFF   |
| read_only             | ON    |
| super_read_only       | ON    |
| transaction_read_only | OFF   |
+-----------------------+-------+
```

> 注：设置 slave 服务器为只读，并不影响主从同步。

## 三、配置

> **_大部分情况下，默认的基本配置已经足够应付大多数场景，不要轻易修改 Mysql 服务器配置，除非你明确知道修改项是有益的。_**

### 配置文件路径

配置 Mysql 首先要确定配置文件在哪儿。

不同 Linux 操作系统上，Mysql 配置文件路径可能不同。通常的路径为 /etc/my.cnf 或 /etc/mysql/my.cnf 。

如果不知道配置文件路径，可以尝试以下操作：

```shell
# which mysqld
/usr/sbin/mysqld
# /usr/sbin/mysqld --verbose --help | grep -A 1 'Default options'
Default options are read from the following files in the given order:
/etc/my.cnf /etc/mysql/my.cnf /usr/etc/my.cnf ~/.my.cnf
```

### 配置项语法

**Mysql 配置项设置都使用小写，单词之间用下划线或横线隔开（二者是等价的）。**

建议使用固定的风格，这样检索配置项时较为方便。

```shell
# 这两种格式等价
/usr/sbin/mysqld --auto-increment-offset=5
/usr/sbin/mysqld --auto_increment_offset=5
```

### 常用配置项说明

> 这里介绍比较常用的基本配置，更多配置项说明可以参考：[Mysql 服务器配置说明](mysql-config.md)

先给出一份常用配置模板，内容如下：

```ini
[mysqld]
# GENERAL
# -------------------------------------------------------------------------------
datadir = /var/lib/mysql
socket  = /var/lib/mysql/mysql.sock
pid_file = /var/lib/mysql/mysql.pid
user = mysql
port = 3306
default_storage_engine = InnoDB
default_time_zone = '+8:00'
character_set_server = utf8mb4
collation_server = utf8mb4_0900_ai_ci

# LOG
# -------------------------------------------------------------------------------
log_error = /var/log/mysql/mysql-error.log
slow_query_log = 1
slow_query_log_file = /var/log/mysql/mysql-slow.log

# InnoDB
# -------------------------------------------------------------------------------
innodb_buffer_pool_size = <value>
innodb_log_file_size = <value>
innodb_file_per_table = 1
innodb_flush_method = O_DIRECT

# MyIsam
# -------------------------------------------------------------------------------
key_buffer_size = <value>

# OTHER
# -------------------------------------------------------------------------------
tmp_table_size = 32M
max_heap_table_size = 32M
query_cache_type = 0
query_cache_size = 0
max_connections = <value>
thread_cache = <value>
open_files_limit = 65535

[client]
socket  = /var/lib/mysql/mysql.sock
port = 3306
```

- GENERAL
  - `datadir` - mysql 数据文件所在目录
  - `socket` - scoket 文件
  - `pid_file` - PID 文件
  - `user` - 启动 mysql 服务进程的用户
  - `port` - 服务端口号，默认 `3306`
  - `default_storage_engine` - mysql 5.1 之后，默认引擎是 InnoDB
  - `default_time_zone` - 默认时区。中国大部分地区在东八区，即 `+8:00`
  - `character_set_server` - 数据库默认字符集
  - `collation_server` - 数据库字符集对应一些排序等规则，注意要和 character_set_server 对应
- LOG
  - `log_error` - 错误日志文件地址
  - `slow_query_log` - 错误日志文件地址
- InnoDB
  - `innodb_buffer_pool_size` - InnoDB 使用一个缓冲池来保存索引和原始数据，不像 MyISAM。这里你设置越大，你在存取表里面数据时所需要的磁盘 I/O 越少。
    - 在一个独立使用的数据库服务器上,你可以设置这个变量到服务器物理内存大小的 60%-80%
    - 注意别设置的过大，会导致 system 的 swap 空间被占用，导致操作系统变慢，从而减低 sql 查询的效率
    - 默认值：128M，建议值：物理内存的 60%-80%
  - `innodb_log_file_size` - 日志文件的大小。默认值：48M，建议值：根据你系统的磁盘空间和日志增长情况调整大小
  - `innodb_file_per_table` - 说明：mysql5.7 之后默认开启，意思是，每张表一个独立表空间。默认值 1，开启。
  - `innodb_flush_method` - 说明：控制着 innodb 数据文件及 redo log 的打开、刷写模式，三种模式：fdatasync(默认)，O_DSYNC，O_DIRECT。默认值为空，建议值：使用 SAN 或者 raid，建议用 O_DIRECT，不懂测试的话，默认生产上使用 O_DIRECT
    - `fdatasync`：数据文件，buffer pool->os buffer->磁盘；日志文件，buffer pool->os buffer->磁盘；
    - `O_DSYNC`： 数据文件，buffer pool->os buffer->磁盘；日志文件，buffer pool->磁盘；
    - `O_DIRECT`： 数据文件，buffer pool->磁盘； 日志文件，buffer pool->os buffer->磁盘；
- MyIsam

  - `key_buffer_size` - 指定索引缓冲区的大小，为 MYISAM 数据表开启供线程共享的索引缓存，对 INNODB 引擎无效。相当影响 MyISAM 的性能。
    - 不要将其设置大于你可用内存的 30%，因为一部分内存同样被 OS 用来缓冲行数据
    - 甚至在你并不使用 MyISAM 表的情况下，你也需要仍旧设置起 8-64M 内存由于它同样会被内部临时磁盘表使用。
    - 默认值 8M，建议值：对于内存在 4GB 左右的服务器该参数可设置为 256M 或 384M。
    - 注意：该参数值设置的过大反而会是服务器整体效率降低！

- OTHER
  - `tmp_table_size` - 内存临时表的最大值，默认 16M，此处设置成 128M
  - `max_heap_table_size` - 用户创建的内存表的大小，默认 16M，往往和 `tmp_table_size` 一起设置，限制用户临时表大小。超限的话，MySQL 就会自动地把它转化为基于磁盘的 MyISAM 表，存储在指定的 tmpdir 目录下，增大 IO 压力，建议内存大，增大该数值。
  - `query_cache_type` - 这个系统变量控制着查询缓存功能的开启和关闭，0 表示关闭，1 表示打开，2 表示只要 `select` 中明确指定 `SQL_CACHE` 才缓存。
  - `query_cache_size` - 默认值 1M，优点是查询缓存可以极大的提高服务器速度，如果你有大量的相同的查询并且很少修改表。缺点：在你表经常变化的情况下或者如果你的查询原文每次都不同，查询缓存也许引起性能下降而不是性能提升。
  - `max_connections` - 最大连接数，可设最大值 16384，一般考虑根据同时在线人数设置一个比较综合的数字，鉴于该数值增大并不太消耗系统资源，建议直接设 10000。如果在访问时经常出现 Too Many Connections 的错误提示，则需要增大该参数值
  - `thread_cache` - 当客户端断开之后，服务器处理此客户的线程将会缓存起来以响应下一个客户而不是销毁。可重用，减小了系统开销。默认值为 9，建议值：两种取值方式，
    - 方式一，根据物理内存，1G —> 8；2G —> 16； 3G —> 32； >3G —> 64；
    - 方式二，根据 show status like 'threads%'，查看 Threads_connected 值。
  - `open_files_limit` - MySQL 打开的文件描述符限制，默认最小 1024;
    - 当 open_files_limit 没有被配置的时候，比较 max_connections\*5 和 ulimit -n 的值，哪个大用哪个，
    - 当 open_file_limit 被配置的时候，比较 open_files_limit 和 max_connections\*5 的值，哪个大用哪个
    - 注意：仍然可能出现报错信息 Can't create a new thread；此时观察系统 cat /proc/mysql 进程号/limits，观察进程 ulimit 限制情况
    - 过小的话，考虑修改系统配置表，`/etc/security/limits.conf` 和 `/etc/security/limits.d/90-nproc.conf`

## 四、常见问题

### Too many connections

**现象**

尝试连接 Mysql 时，遇到 `Too many connections` 错误。

**原因**

数据库连接线程数超过最大值，访问被拒绝。

**解决方案**

如果实际连接线程数过大，可以考虑增加服务器节点来分流；如果实际线程数并不算过大，那么可以配置 `max_connections` 来增加允许的最大连接数。需要注意的是，连接数不宜过大，一般来说，单库每秒有 2000 个并发连接时，就可以考虑扩容了，健康的状态应该维持在每秒 1000 个并发连接左右。

（1）查看最大连接数

```sql
mysql> show variables like '%max_connections%';
+------------------------+-------+
| Variable_name          | Value |
+------------------------+-------+
| max_connections        | 151   |
| mysqlx_max_connections | 100   |
+------------------------+-------+
```

（2）查看服务器响应的最大连接数

```sql
mysql> show global status like 'Max_used_connections';
+----------------------+-------+
| Variable_name        | Value |
+----------------------+-------+
| Max_used_connections | 142   |
+----------------------+-------+
1 row in set (0.00 sec)
```

（3）临时设置最大连接数

```sql
set GLOBAL max_connections=256;
```

注意：当服务器重启时，最大连接数会被重置。

（4）永久设置最大连接数

修改 `/etc/my.cnf` 配置文件，在 `[mysqld]` 添加以下配置：

```sql
max_connections=256
```

重启 mysql 以生效

（5）修改 Linux 最大文件数限制

设置了最大连接数，如果还是没有生效，考虑检查一下 Linux 最大文件数

Mysql 最大连接数会受到最大文件数限制，`vim /etc/security/limits.conf`，添加 mysql 用户配置

```
mysql hard nofile 65535
mysql soft nofile 65535
```

（6）检查 LimitNOFILE

如果是使用 rpm 方式安装 mysql，检查 **mysqld.service** 文件中的 `LimitNOFILE` 是否配置的太小。

### 时区（time_zone）偏差

**现象**

数据库中存储的 Timestamp 字段值比真实值少了 13 个小时。

**原因**

- 当 JDBC 与 MySQL 开始建立连接时，会获取服务器参数。
- 当 MySQL 的 `time_zone` 值为 `SYSTEM` 时，会取 `system_time_zone` 值作为协调时区，若得到的是 `CST` 那么 Java 会误以为这是 `CST -0500` ，因此会给出错误的时区信息（国内一般是`CST +0800`，即东八区）。

查看时区方法：

通过 `show variables like '%time_zone%';` 命令查看 Mysql 时区配置：

```sql
mysql> show variables like '%time_zone%';
+------------------+--------+
| Variable_name    | Value  |
+------------------+--------+
| system_time_zone | CST    |
| time_zone        | SYSTEM |
+------------------+--------+
```

**解决方案**

方案一

```sql
mysql> set global time_zone = '+08:00';
Query OK, 0 rows affected (0.00 sec)

mysql> set time_zone = '+08:00';
Query OK, 0 rows affected (0.00 sec)
```

方案二

修改 `my.cnf` 文件，在 `[mysqld]` 节下增加 `default-time-zone='+08:00'` ，然后重启。

### 数据表损坏如何修复

使用 myisamchk 来修复，具体步骤：

1. 修复前将 mysql 服务停止。
2. 打开命令行方式，然后进入到 mysql 的 `bin` 目录。
3. 执行 myisamchk –recover 数据库所在路 /\*.MYI

使用 repair table 或者 OPTIMIZE table 命令来修复，REPAIR TABLE table_name 修复表 OPTIMIZE TABLE table_name 优化表 REPAIR TABLE 用于修复被破坏的表。 OPTIMIZE TABLE 用于回收闲置的数据库空间，当表上的数据行被删除时，所占据的磁盘空间并没有立即被回收，使用了 OPTIMIZE TABLE 命令后这些空间将被回收，并且对磁盘上的数据行进行重排（注意：是磁盘上，而非数据库）

### 数据结构

> 问题现象：ERROR 1071: Specified key was too long; max key length is 767 bytes

问题原因：Mysql 默认情况下单个列的索引不能超过 767 位（不同版本可能存在差异） 。

解决方法：优化索引结构，索引字段不宜过长。

## 五、脚本

这里推荐我写的几个一键运维脚本，非常方便，欢迎使用：

- [Mysql 安装脚本](https://github.com/dunwu/linux-tutorial/tree/master/codes/linux/soft/mysql-install.sh)
- [Mysql 备份脚本](https://github.com/dunwu/linux-tutorial/tree/master/codes/linux/soft/mysql-backup.sh)

## 参考资料

- [《高性能 MySQL》](https://book.douban.com/subject/23008813/)
- https://www.cnblogs.com/xiaopotian/p/8196464.html
- https://www.cnblogs.com/bigbrotherer/p/7241845.html
- https://blog.csdn.net/managementandjava/article/details/80039650
- http://www.manongjc.com/article/6996.html
- https://www.cnblogs.com/xyabk/p/8967990.html
- [MySQL 8.0 主从（Master-Slave）配置](https://blog.csdn.net/zyhlwzy/article/details/80569422)
- [Mysql 主从同步实战](https://juejin.im/post/58eb5d162f301e00624f014a)
- [MySQL 备份和恢复机制](https://juejin.im/entry/5a0aa2026fb9a045132a369f)
