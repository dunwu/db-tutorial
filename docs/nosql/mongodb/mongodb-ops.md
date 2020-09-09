# MongoDB 运维

<!-- TOC depthFrom:2 depthTo:3 -->

- [一、MongoDB 安装](#一mongodb-安装)
    - [Windows](#windows)
    - [Linux](#linux)
    - [设置用户名、密码](#设置用户名密码)
- [参考资料](#参考资料)

<!-- /TOC -->

## 一、MongoDB 安装

### Windows

（1）下载并解压到本地

进入官网下载地址：[**官方下载地址**](https://www.mongodb.com/try/download/community) ，选择合适的版本下载。

（2）创建数据目录

MongoDB 将数据目录存储在 db 目录下。但是这个数据目录不会主动创建，我们在安装完成后需要创建它。

例如：`D:\Tools\Server\mongodb\mongodb-4.4.0\data\db`

（3）运行 MongoDB 服务

```shell
mongod --dbpath D:\Tools\Server\mongodb\mongodb-4.4.0\data\db
```

（4）客户端连接 MongoDB

可以在命令窗口中运行 mongo.exe 命令即可连接上 MongoDB

（5）配置 MongoDB 服务

### Linux

（1）使用安装包安装

安装前我们需要安装各个 Linux 平台依赖包。

**Red Hat/CentOS：**

```
sudo yum install libcurl openssl
```

**Ubuntu 18.04 LTS ("Bionic")/Debian 10 "Buster"：**

```
sudo apt-get install libcurl4 openssl
```

**Ubuntu 16.04 LTS ("Xenial")/Debian 9 "Stretch"：**

```
sudo apt-get install libcurl3 openssl
```

（2）创建数据目录

默认情况下 MongoDB 启动后会初始化以下两个目录：

- 数据存储目录：/var/lib/mongodb
- 日志文件目录：/var/log/mongodb

我们在启动前可以先创建这两个目录并设置当前用户有读写权限：

```
sudo mkdir -p /var/lib/mongo
sudo mkdir -p /var/log/mongodb
sudo chown `whoami` /var/lib/mongo     # 设置权限
sudo chown `whoami` /var/log/mongodb   # 设置权限
```

（3）运行 MongoDB 服务

```
mongod --dbpath /var/lib/mongo --logpath /var/log/mongodb/mongod.log --fork
```

打开 /var/log/mongodb/mongod.log 文件看到以下信息，说明启动成功。

```
# tail -10f /var/log/mongodb/mongod.log
2020-07-09T12:20:17.391+0800 I  NETWORK  [listener] Listening on /tmp/mongodb-27017.sock
2020-07-09T12:20:17.392+0800 I  NETWORK  [listener] Listening on 127.0.0.1
2020-07-09T12:20:17.392+0800 I  NETWORK  [listener] waiting for connections on port 27017
```

（4）客户端连接 MongoDB

```
cd /usr/local/mongodb4/bin
./mongo
```

> [Linux 安装脚本](https://github.com/dunwu/linux-tutorial/tree/master/codes/linux/soft)

### 设置用户名、密码

```
> use admin
switched to db admin
> db.createUser({"user":"root","pwd":"root","roles":[{"role":"userAdminAnyDatabase","db":"admin"}]})
Successfully added user: {
        "user" : "root",
        "roles" : [
                {
                        "role" : "userAdminAnyDatabase",
                        "db" : "admin"
                }
        ]
}
>
```

## 参考资料

- [MongoDB 官网](https://www.mongodb.com/)
- [MongoDBGithub](https://github.com/mongodb/mongo)
- [MongoDB 官方免费教程](https://university.mongodb.com/)
- [MongoDB 教程](https://www.runoob.com/mongodb/mongodb-tutorial.html)
