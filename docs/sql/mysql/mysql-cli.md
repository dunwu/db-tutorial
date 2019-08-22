# Mysql 命令

<!-- TOC depthFrom:2 depthTo:3 -->

- [登录 mysql](#登录-mysql)
    - [无密码登录](#无密码登录)
    - [有密码登录](#有密码登录)
    - [远程登录](#远程登录)
- [账户](#账户)
    - [更改 root 密码](#更改-root-密码)
- [数据管理](#数据管理)
    - [清空所有表（数据库名是 test）](#清空所有表数据库名是-test)
- [备份和恢复](#备份和恢复)
    - [数据库备份](#数据库备份)
    - [数据库恢复](#数据库恢复)
- [:door: 传送门](#door-传送门)

<!-- /TOC -->

## 登录 mysql

语法：

```bash
mysql -D 数据库名 -h 主机名 -u 用户名 -p '密码'
```

### 无密码登录

```bash
mysql -uroot
```

### 有密码登录

```bash
mysql -u root -p'yourpassword'
```

### 远程登录

```bash
mysql -uroot -p'yourpassword' -h<ip> -P3306
```

## 账户

### 更改 root 密码

```bash
mysqladmin -uroot password 'yourpassword'
```

## 数据管理

### 清空所有表（数据库名是 test）

```bash
mysql -N -s information_schema -e "SELECT CONCAT('TRUNCATE TABLE ',TABLE_NAME,';') FROM TABLES WHERE TABLE_SCHEMA='test'" | mysql -f test
```

## 备份和恢复

### 数据库备份

备份所有数据库到指定位置：

```bash
mysqldump -u root -p'yourpassword' -f --all-databases > /home/zp/sql/all.sql
```

备份指定数据库到指定位置：

```bash
mysqldump -u root -p'yourpassword' <database1> <database2> <database3> > /home/zp/sql/all.sql
```

远程备份

```bash
mysqldump -u root -p'yourpassword' -h<ip> mysql >/tmp/mysql.sql
```

### 数据库恢复

```bash
mysql -u root -p'yourpassword' mysql < /home/zp/sql/all.sql
```

## :door: 传送门

| [我的 Github 博客](https://github.com/dunwu/blog) | [db-tutorial 首页](https://github.com/dunwu/db-tutorial) |
