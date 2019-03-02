# Mysql 维护

<!-- TOC depthFrom:2 depthTo:3 -->

- [安装配置](#安装配置)
    - [安装 mysql yum 源](#安装-mysql-yum-源)
    - [安装 mysql 服务器](#安装-mysql-服务器)
    - [启动 mysql 服务](#启动-mysql-服务)
    - [初始化数据库密码](#初始化数据库密码)
    - [配置远程访问](#配置远程访问)
    - [跳过登录认证](#跳过登录认证)
- [运维](#运维)
- [备份与恢复](#备份与恢复)
    - [备份](#备份)
    - [恢复](#恢复)
- [卸载](#卸载)
- [问题](#问题)
    - [JDBC 与 Mysql 因 CST 时区协商无解导致偏差了 14 或 13 小时](#jdbc-与-mysql-因-cst-时区协商无解导致偏差了-14-或-13-小时)
- [参考资料](#参考资料)

<!-- /TOC -->

## 安装配置

通过 rpm 包安装

centos 的 yum 源中默认是没有 mysql 的，所以我们需要先去官网下载 mysql 的 repo 源并安装。

### 安装 mysql yum 源

官方下载地址：https://dev.mysql.com/downloads/repo/yum/

（1）下载 yum 源

```bash
$ wget https://dev.mysql.com/get/mysql80-community-release-el7-1.noarch.rpm
```

（2）安装 yum repo 文件并更新 yum 缓存

```bash
$ rpm -ivh mysql80-community-release-el7-1.noarch.rpm
```

执行结果：

会在 /etc/yum.repos.d/ 目录下生成两个 repo 文件

```bash
$ ls | grep mysql
mysql-community.repo
mysql-community-source.repo
```

更新 yum：

```bash
$ yum clean all
$ yum makecache
```

（3）查看 rpm 安装状态

```bash
$ yum search mysql | grep server
mysql-community-common.i686 : MySQL database common files for server and client
mysql-community-common.x86_64 : MySQL database common files for server and
mysql-community-test.x86_64 : Test suite for the MySQL database server
                       : administering MySQL servers
mysql-community-server.x86_64 : A very fast and reliable SQL database server
```

通过 yum 安装 mysql 有几个重要目录：

```
# 数据库目录
/var/lib/mysql/
# 配置文件
/usr/share/mysql（mysql.server命令及配置文件）
# 相关命令
/usr/bin（mysqladmin mysqldump等命令）
# 启动脚本
/etc/rc.d/init.d/（启动脚本文件mysql的目录）
# 配置文件
/etc/my.cnf
```

### 安装 mysql 服务器

```bash
$ yum install mysql-community-server
```

### 启动 mysql 服务

```bash
# 启动 mysql 服务
$ systemctl start mysqld.service

# 查看运行状态
$ systemctl status mysqld.service

# 开机启动
$ systemctl enable mysqld
$ systemctl daemon-reload
```

### 初始化数据库密码

查看一下初始密码

```bash
$ grep "password" /var/log/mysqld.log
2018-09-30T03:13:41.727736Z 5 [Note] [MY-010454] [Server] A temporary password is generated for root@localhost: %:lt+srWu4k1
```

执行命令：

```bash
mysql -uroot -p
```

输入临时密码，进入 mysql

```bash
ALTER user 'root'@'localhost' IDENTIFIED BY 'Tw#123456';
```

注：密码强度默认为中等，大小写字母、数字、特殊符号，只有修改成功后才能修改配置再设置更简单的密码

### 配置远程访问

```
GRANT ALL ON *.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### 跳过登录认证

```
vim /etc/my.cnf
```

在 [mysqld] 下面加上 skip-grant-tables

作用是登录时跳过登录认证，换句话说就是 root 什么密码都可以登录进去。

执行 `service mysqld restart`，重启 mysql

## 运维

## 备份与恢复

Mysql 备份数据使用 mysqldump 命令。

mysqldump 将数据库中的数据备份成一个文本文件，表的结构和表中的数据将存储在生成的文本文件中。

### 备份

（1）备份一个数据库

语法：

```
mysqldump -u <username> -p <database> [<table1> <table2> ...] > backup.sql
```

- username 数据库用户
- dbname 数据库名称
- table1 和 table2 参数表示需要备份的表的名称，为空则整个数据库备份；
- BackupName.sql 参数表设计备份文件的名称，文件名前面可以加上一个绝对路径。通常将数据库被分成一个后缀名为 sql 的文件

（2）备份多个数据库

```
mysqldump -u <username> -p --databases <database1> <database2> ... > backup.sql
```

（3）备份所有数据库

```
mysqldump -u <username> -p -all-databases > backup.sql
```

### 恢复

Mysql 恢复数据使用 mysqldump 命令。

语法：

```
mysql -u <username> -p <database> < backup.sql
```

## 卸载

（1）查看已安装的 mysql

```bash
$ rpm -qa | grep -i mysql
perl-DBD-MySQL-4.023-6.el7.x86_64
mysql80-community-release-el7-1.noarch
mysql-community-common-8.0.12-1.el7.x86_64
mysql-community-client-8.0.12-1.el7.x86_64
mysql-community-libs-compat-8.0.12-1.el7.x86_64
mysql-community-libs-8.0.12-1.el7.x86_64
```

（2）卸载 mysql

```bash
$ yum remove mysql-community-server.x86_64
```

## 问题

### JDBC 与 Mysql 因 CST 时区协商无解导致偏差了 14 或 13 小时

**现象**

数据库中存储的 Timestamp 字段值比真实值少了 13 个小时。

**原因**

- 当 JDBC 与 MySQL 开始建立连接时，会获取服务器参数。
- 当 MySQL 的 `time_zone` 值为 `SYSTEM` 时，会取 `system_time_zone` 值作为协调时区，若得到的是 `CST` 那么 Java 会误以为这是 `CST -0500` ，因此会给出错误的时区信息（国内一般是`CST +0800`，即东八区）。

> 查看时区方法：
>
> 通过 `show variables like '%time_zone%';` 命令查看 Mysql 时区配置：
>
> ```
> mysql> show variables like '%time_zone%';
> +------------------+--------+
> | Variable_name    | Value  |
> +------------------+--------+
> | system_time_zone | CST    |
> | time_zone        | SYSTEM |
> +------------------+--------+
> ```

**解决方案**

方案一

```
mysql> set global time_zone = '+08:00';
Query OK, 0 rows affected (0.00 sec)

mysql> set time_zone = '+08:00';
Query OK, 0 rows affected (0.00 sec)
```

方案二

修改 `my.cnf` 文件，在 `[mysqld]` 节下增加 `default-time-zone = '+08:00'` ，然后重启。

## 参考资料

https://www.cnblogs.com/xiaopotian/p/8196464.html
https://www.cnblogs.com/bigbrotherer/p/7241845.html
https://blog.csdn.net/managementandjava/article/details/80039650
http://www.manongjc.com/article/6996.html
https://www.cnblogs.com/xyabk/p/8967990.html
