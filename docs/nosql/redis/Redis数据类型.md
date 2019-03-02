---
title: Redis 数据类型
date: 2018/06/09
categories:
- database
tags:
- database
- nosql
---

# Redis 数据类型

<!-- TOC depthFrom:2 depthTo:3 -->

- [STRING](#string)
- [LIST](#list)
- [SET](#set)
- [HASH](#hash)
- [ZSET](#zset)

<!-- /TOC -->

| 数据类型 |      可以存储的值      |                                                       操作                                                       |
| :------: | :--------------------: | :--------------------------------------------------------------------------------------------------------------: |
|  STRING  | 字符串、整数或者浮点数 |                对整个字符串或者字符串的其中一部分执行操作</br> 对整数和浮点数执行自增或者自减操作                |
|   LIST   |          列表          |              从两端压入或者弹出元素</br> 读取单个或者多个元素</br> 进行修剪，只保留一个范围内的元素              |
|   SET    |        无序集合        | 添加、获取、移除单个元素</br> 检查一个元素是否存在于集合中</br> 计算交集、并集、差集</br> 从集合里面随机获取元素 |
|   HASH   | 包含键值对的无序散列表 |                      添加、获取、移除单个键值对</br> 获取所有键值对</br> 检查某个键是否存在                      |
|   ZSET   |        有序集合        |                  添加、获取、删除元素</br> 根据分值范围或者成员来获取元素</br> 计算一个键的排名                  |

## STRING

<div align="center">
<img src="https://raw.githubusercontent.com/dunwu/images/master/images/database/redis/redis-datatype-string.png" width="400"/>
</div>

命令：

| 命令 | 行为                                               |
| ---- | -------------------------------------------------- |
| GET  | 获取存储在给定键中的值                             |
| SET  | 设置存储在给定键中的值                             |
| DEL  | 删除存储在给定键中的值（这个命令可以用于所有类型） |

示例：

```py
127.0.0.1:6379> set name jack
OK
127.0.0.1:6379> get name
"jack"
127.0.0.1:6379> del name
(integer) 1
127.0.0.1:6379> get name
(nil)
```

## LIST

<div align="center">
<img src="https://raw.githubusercontent.com/dunwu/images/master/images/database/redis/redis-datatype-list.png" width="400"/>
</div>

命令：

| 命令   | 行为                                     |
| ------ | ---------------------------------------- |
| RPUSH  | 将给定值推入列表的右端                   |
| LRANGE | 获取列表在给定范围上的所有值             |
| LINDEX | 获取列表在给定位置上的单个元素           |
| LPOP   | 从列表的左端弹出一个值，并返回被弹出的值 |

示例：

```py
127.0.0.1:6379> rpush list item1
(integer) 1
127.0.0.1:6379> rpush list item2
(integer) 2
127.0.0.1:6379> rpush list item3
(integer) 3
127.0.0.1:6379> lrange list 0 -1
1) "item1"
2) "item2"
3) "item3"
127.0.0.1:6379> lindex list 1
"item2"
127.0.0.1:6379> lpop list
"item1"
127.0.0.1:6379> lrange list 0 -1
1) "item2"
2) "item3"
```

## SET

<div align="center">
<img src="https://raw.githubusercontent.com/dunwu/images/master/images/database/redis/redis-datatype-set.png" width="400"/>
</div>

命令：

| 命令      | 行为                                         |
| --------- | -------------------------------------------- |
| SADD      | 将给定元素添加到集合                         |
| SMEMBERS  | 返回集合包含的所有元素                       |
| SISMEMBER | 检查给定元素是否存在于集合中                 |
| SREM      | 如果给定的元素存在于集合中，那么移除这个元素 |

示例：

```py
127.0.0.1:6379> sadd set item1
(integer) 1
127.0.0.1:6379> sadd set item2
(integer) 1
127.0.0.1:6379> sadd set item3
(integer) 1
127.0.0.1:6379> sadd set item3
(integer) 0

127.0.0.1:6379> smembers set
1) "item3"
2) "item2"
3) "item1"

127.0.0.1:6379> sismember set item2
(integer) 1
127.0.0.1:6379> sismember set item6
(integer) 0

127.0.0.1:6379> srem set item2
(integer) 1
127.0.0.1:6379> srem set item2
(integer) 0

127.0.0.1:6379> smembers set
1) "item3"
2) "item1"
```

## HASH

<div align="center">
<img src="https://raw.githubusercontent.com/dunwu/images/master/images/database/redis/redis-datatype-hash.png" width="400"/>
</div>

命令：

| 命令    | 行为                                     |
| ------- | ---------------------------------------- |
| HSET    | 在散列里面关联起给定的键值对             |
| HGET    | 获取指定散列键的值                       |
| HGETALL | 获取散列包含的所有键值对                 |
| HDEL    | 如果给定键存在于散列里面，那么移除这个键 |

示例：

```py
127.0.0.1:6379> hset myhash key1 value1
(integer) 1
127.0.0.1:6379> hset myhash key2 value2
(integer) 1
127.0.0.1:6379> hset myhash key3 value3
(integer) 1
127.0.0.1:6379> hset myhash key3 value2
(integer) 0

127.0.0.1:6379> hgetall myhash
1) "key1"
2) "value1"
3) "key2"
4) "value2"
5) "key3"
6) "value2"

127.0.0.1:6379> hdel myhash key2
(integer) 1
127.0.0.1:6379> hdel myhash key2
(integer) 0

127.0.0.1:6379> hget myhash key2
(nil)

127.0.0.1:6379> hgetall myhash
1) "key1"
2) "value1"
3) "key3"
4) "value2"
127.0.0.1:6379>
```

## ZSET

<div align="center">
<img src="https://raw.githubusercontent.com/dunwu/images/master/images/database/redis/redis-datatype-zset.png" width="400"/>
</div>

命令：

| 命令          | 行为                                                       |
| ------------- | ---------------------------------------------------------- |
| ZADD          | 将一个带有给定分值得成员添加到有序集合里面                 |
| ZRANGE        | 根据元素在有序排列中所处的位置，从有序集合里面获取多个元素 |
| ZRANGEBYSCORE | 获取有序集合在给定分值范围内的所有元素                     |
| ZREM          | 如果给定成员存在于有序集合，那么移除这个成员               |

示例：

```py
127.0.0.1:6379> zadd zset 1 redis
(integer) 1
127.0.0.1:6379> zadd zset 2 mongodb
(integer) 1
127.0.0.1:6379> zadd zset 3 mysql
(integer) 1
127.0.0.1:6379> zadd zset 3 mysql
(integer) 0
127.0.0.1:6379> zadd zset 4 mysql
(integer) 0

127.0.0.1:6379> zrange zset 0 -1 withscores
1) "redis"
2) "1"
3) "mongodb"
4) "2"
5) "mysql"
6) "4"

127.0.0.1:6379> zrangebyscore zset 0 2 withscores
1) "redis"
2) "1"
3) "mongodb"
4) "2"

127.0.0.1:6379> zrem zset mysql
(integer) 1
127.0.0.1:6379> zrange zset 0 -1 withscores
1) "redis"
2) "1"
3) "mongodb"
4) "2"
```
