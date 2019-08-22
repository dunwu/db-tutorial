# PostgreSQL 快速指南

> [PostgreSQL](https://www.postgresql.org/) 是一个关系型数据库（RDBM）。
>
> 关键词：Database, RDBM, psql

<br><div align="center"><img src="http://dunwu.test.upcdn.net/snap/20180920181010182614.png!zp"/></div><br>

<!-- TOC depthFrom:2 depthTo:3 -->

- [安装](#安装)
- [添加新用户和新数据库](#添加新用户和新数据库)
- [登录数据库](#登录数据库)
- [控制台命令](#控制台命令)
- [数据库操作](#数据库操作)
- [备份和恢复](#备份和恢复)
- [参考资料](#参考资料)

<!-- /TOC -->

## 安装

> 本文仅以运行在 Centos 环境下举例。

进入[官方下载页面](https://www.postgresql.org/download/)，根据操作系统选择合适版本。

官方下载页面要求用户选择相应版本，然后动态的给出安装提示，如下图所示：

<br><div align="center"><img src="http://dunwu.test.upcdn.net/snap/20180920181010174348.png!zp"/></div><br>

前 3 步要求用户选择，后 4 步是根据选择动态提示的安装步骤

（1）选择 PostgreSQL 版本

（2）选择平台

（3）选择架构

（4）安装 PostgreSQL 的 rpm 仓库（为了识别下载源）

```sh
yum install https://download.postgresql.org/pub/repos/yum/10/redhat/rhel-7-x86_64/pgdg-centos10-10-2.noarch.rpm
```

（5）安装客户端

```sh
yum install postgresql10
```

（6）安装服务端（可选的）

```sh
yum install postgresql10-server
```

（7）设置开机启动（可选的）

```sh
/usr/pgsql-10/bin/postgresql-10-setup initdb
systemctl enable postgresql-10
systemctl start postgresql-10
```

## 添加新用户和新数据库

初次安装后，默认生成一个名为 postgres 的数据库和一个名为 postgres 的数据库用户。这里需要注意的是，同时还生成了一个名为 postgres 的 Linux 系统用户。

首先，新建一个 Linux 新用户，可以取你想要的名字，这里为 dbuser。

```
sudo adduser dbuser
```

使用 psql 命令登录 PostgreSQL 控制台：

```
sudo -u postgres psql
```

这时相当于系统用户 postgres 以同名数据库用户的身份，登录数据库，这是不用输入密码的。如果一切正常，系统提示符会变为"postgres=#"，表示这时已经进入了数据库控制台。以下的命令都在控制台内完成。

（1）使用 `\password` 命令，为 postgres 用户设置一个密码。

```
postgres=# \password postgres
```

（2）创建数据库用户 dbuser（刚才创建的是 Linux 系统用户），并设置密码。

```sql
CREATE USER dbuser WITH PASSWORD 'password';
```

（3）创建用户数据库，这里为 exampledb，并指定所有者为 dbuser。

```sql
CREATE DATABASE exampledb OWNER dbuser;
```

（4）将 exampledb 数据库的所有权限都赋予 dbuser，否则 dbuser 只能登录控制台，没有任何数据库操作权限。

```sql
GRANT ALL PRIVILEGES ON DATABASE exampledb to dbuser;
```

（5）使用\q 命令退出控制台（也可以直接按 ctrl+D）。

## 登录数据库

添加新用户和新数据库以后，就要以新用户的名义登录数据库，这时使用的是 psql 命令。

```
psql -U dbuser -d exampledb -h 127.0.0.1 -p 5432
```

上面命令的参数含义如下：-U 指定用户，-d 指定数据库，-h 指定服务器，-p 指定端口。

输入上面命令以后，系统会提示输入 dbuser 用户的密码。输入正确，就可以登录控制台了。

psql 命令存在简写形式。如果当前 Linux 系统用户，同时也是 PostgreSQL 用户，则可以省略用户名（-U 参数的部分）。举例来说，我的 Linux 系统用户名为 ruanyf，且 PostgreSQL 数据库存在同名用户，则我以 ruanyf 身份登录 Linux 系统后，可以直接使用下面的命令登录数据库，且不需要密码。

```
psql exampledb
```

此时，如果 PostgreSQL 内部还存在与当前系统用户同名的数据库，则连数据库名都可以省略。比如，假定存在一个叫做 ruanyf 的数据库，则直接键入 psql 就可以登录该数据库。

psql

另外，如果要恢复外部数据，可以使用下面的命令。

```
psql exampledb < exampledb.sql
```

## 控制台命令

除了前面已经用到的 \password 命令（设置密码）和 \q 命令（退出）以外，控制台还提供一系列其他命令。

```
\password           设置密码
\q                  退出
\h                  查看SQL命令的解释，比如\h select
\?                  查看psql命令列表
\l                  列出所有数据库
\c [database_name]  连接其他数据库
\d                  列出当前数据库的所有表格
\d [table_name]     列出某一张表格的结构
\x                  对数据做展开操作
\du                 列出所有用户
```

## 数据库操作

基本的数据库操作，就是使用一般的 SQL 语言。

```sql
# 创建新表
CREATE TABLE user_tbl(name VARCHAR(20), signup_date DATE);
# 插入数据
INSERT INTO user_tbl(name, signup_date) VALUES('张三', '2013-12-22');
# 选择记录
SELECT * FROM user_tbl;
# 更新数据
UPDATE user_tbl set name = '李四' WHERE name = '张三';
# 删除记录
DELETE FROM user_tbl WHERE name = '李四' ;
# 添加栏位
ALTER TABLE user_tbl ADD email VARCHAR(40);
# 更新结构
ALTER TABLE user_tbl ALTER COLUMN signup_date SET NOT NULL;
# 更名栏位
ALTER TABLE user_tbl RENAME COLUMN signup_date TO signup;
# 删除栏位
ALTER TABLE user_tbl DROP COLUMN email;
# 表格更名
ALTER TABLE user_tbl RENAME TO backup_tbl;
# 删除表格
DROP TABLE IF EXISTS backup_tbl;
```

## 备份和恢复

```sh
$ pg_dump --format=t -d db_name -U user_name -h 127.0.0.1 -O -W  > dump.sql
$ psql -h 127.0.0.1 -U user_name db_name < dump.sql
```

## 参考资料

- https://www.postgresql.org/download/
- http://www.ruanyifeng.com/blog/2013/12/getting_started_with_postgresql.html

## :door: 传送门

| [我的 Github 博客](https://github.com/dunwu/blog) | [db-tutorial 首页](https://github.com/dunwu/db-tutorial) |
