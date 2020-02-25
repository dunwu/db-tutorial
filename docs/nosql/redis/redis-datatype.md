# Redis 数据类型

> Redis 提供了多种数据类型，每种数据类型有丰富的命令支持。
>
> 使用 Redis ，不仅要了解其数据类型的特性，还需要根据业务场景，灵活的、高效的使用其数据类型来建模。

## Redis 数据类型简介

| 数据类型 | 可以存储的值           | 操作                                                                                                             |
| -------- | ---------------------- | ---------------------------------------------------------------------------------------------------------------- |
| STRING   | 字符串、整数或者浮点数 | 对整个字符串或者字符串的其中一部分执行操作</br> 对整数和浮点数执行自增或者自减操作                               |
| LIST     | 列表                   | 从两端压入或者弹出元素</br> 读取单个或者多个元素</br> 进行修剪，只保留一个范围内的元素                           |
| SET      | 无序集合               | 添加、获取、移除单个元素</br> 检查一个元素是否存在于集合中</br> 计算交集、并集、差集</br> 从集合里面随机获取元素 |
| HASH     | 包含键值对的无序散列表 | 添加、获取、移除单个键值对</br> 获取所有键值对</br> 检查某个键是否存在                                           |
| ZSET     | 有序集合               | 添加、获取、删除元素</br> 根据分值范围或者成员来获取元素</br> 计算一个键的排名                                   |

> [What Redis data structures look like](https://redislabs.com/ebook/part-1-getting-started/chapter-1-getting-to-know-redis/1-2-what-redis-data-structures-look-like/)

### STRING

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-string.png" width="400"/>
</div>

应用场景：缓存、计数器、共享 Session

命令：

| 命令  | 行为                                                 |
| ----- | ---------------------------------------------------- |
| `GET` | 获取存储在给定键中的值。                             |
| `SET` | 设置存储在给定键中的值。                             |
| `DEL` | 删除存储在给定键中的值（这个命令可以用于所有类型）。 |

> 更多命令请参考：[Redis String 类型命令](https://redis.io/commands#string)

示例：

```shell
127.0.0.1:6379> set hello world
OK
127.0.0.1:6379> get hello
"jack"
127.0.0.1:6379> del hello
(integer) 1
127.0.0.1:6379> get hello
(nil)
```

### HASH

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-hash.png" width="400"/>
</div>

场景：适合存储结构化数据，如一个对象：用户信息、产品信息等。

命令：

| 命令      | 行为                                       |
| --------- | ------------------------------------------ |
| `HSET`    | 在散列里面关联起给定的键值对。             |
| `HGET`    | 获取指定散列键的值。                       |
| `HGETALL` | 获取散列包含的所有键值对。                 |
| `HDEL`    | 如果给定键存在于散列里面，那么移除这个键。 |

> 更多命令请参考：[Redis Hash 类型命令](https://redis.io/commands#hash)

示例：

```shell
127.0.0.1:6379> hset hash-key sub-key1 value1
(integer) 1
127.0.0.1:6379> hset hash-key sub-key2 value2
(integer) 1
127.0.0.1:6379> hset hash-key sub-key1 value1
(integer) 0
127.0.0.1:6379> hset hash-key sub-key3 value2
(integer) 0
127.0.0.1:6379> hgetall hash-key
1) "sub-key1"
2) "value1"
3) "sub-key2"
4) "value2"
127.0.0.1:6379> hdel hash-key sub-key2
(integer) 1
127.0.0.1:6379> hdel hash-key sub-key2
(integer) 0
127.0.0.1:6379> hget hash-key sub-key1
"value1"
127.0.0.1:6379> hgetall hash-key
1) "sub-key1"
2) "value1"
```

### LIST

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-list.png" width="400"/>
</div>

适用场景：用于存储列表型数据。如：粉丝列表、商品列表等。

命令：

| 命令     | 行为                                       |
| -------- | ------------------------------------------ |
| `RPUSH`  | 将给定值推入列表的右端。                   |
| `LRANGE` | 获取列表在给定范围上的所有值。             |
| `LINDEX` | 获取列表在给定位置上的单个元素。           |
| `LPOP`   | 从列表的左端弹出一个值，并返回被弹出的值。 |

> 更多命令请参考：[Redis List 类型命令](https://redis.io/commands#list)

示例：

```shell
127.0.0.1:6379> rpush list-key item
(integer) 1
127.0.0.1:6379> rpush list-key item2
(integer) 2
127.0.0.1:6379> rpush list-key item
(integer) 3
127.0.0.1:6379> lrange list-key 0 -1
1) "item"
2) "item2"
3) "item"
127.0.0.1:6379> lindex list-key 1
"item2"
127.0.0.1:6379> lpop list-key
"item"
127.0.0.1:6379> lrange list-key 0 -1
1) "item2"
2) "item"
```

### SET

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-set.png" width="400"/>
</div>

适用场景：适用于存储不出现重复的列表数据。

命令：

| 命令        | 行为                                           |
| ----------- | ---------------------------------------------- |
| `SADD`      | 将给定元素添加到集合。                         |
| `SMEMBERS`  | 返回集合包含的所有元素。                       |
| `SISMEMBER` | 检查给定元素是否存在于集合中。                 |
| `SREM`      | 如果给定的元素存在于集合中，那么移除这个元素。 |

> 更多命令请参考：[Redis Set 类型命令](https://redis.io/commands#set)

示例：

```shell
127.0.0.1:6379> sadd set-key item
(integer) 1
127.0.0.1:6379> sadd set-key item2
(integer) 1
127.0.0.1:6379> sadd set-key item3
(integer) 1
127.0.0.1:6379> sadd set-key item
(integer) 0
127.0.0.1:6379> smembers set-key
1) "item"
2) "item2"
3) "item3"
127.0.0.1:6379> sismember set-key item4
(integer) 0
127.0.0.1:6379> sismember set-key item
(integer) 1
127.0.0.1:6379> srem set-key item2
(integer) 1
127.0.0.1:6379> srem set-key item2
(integer) 0
127.0.0.1:6379> smembers set-key
1) "item"
2) "item3"
```

### ZSET

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-zset.png" width="400"/>
</div>

场景：由于可以设置 score，且不重复。适合存储各种排行数据，如：按评分排序的有序商品集合、按时间排序的有序文章集合。

命令：

| 命令            | 行为                                                         |
| --------------- | ------------------------------------------------------------ |
| `ZADD`          | 将一个带有给定分值的成员添加到有序集合里面。                 |
| `ZRANGE`        | 根据元素在有序排列中所处的位置，从有序集合里面获取多个元素。 |
| `ZRANGEBYSCORE` | 获取有序集合在给定分值范围内的所有元素。                     |
| `ZREM`          | 如果给定成员存在于有序集合，那么移除这个成员。               |

> 更多命令请参考：[Redis ZSet 类型命令](https://redis.io/commands#sorted_set)

示例：

```shell
127.0.0.1:6379> zadd zset-key 728 member1
(integer) 1
127.0.0.1:6379> zadd zset-key 982 member0
(integer) 1
127.0.0.1:6379> zadd zset-key 982 member0
(integer) 0

127.0.0.1:6379> zrange zset-key 0 -1 withscores
1) "member1"
2) "728"
3) "member0"
4) "982"

127.0.0.1:6379> zrangebyscore zset-key 0 800 withscores
1) "member1"
2) "728"

127.0.0.1:6379> zrem zset-key member1
(integer) 1
127.0.0.1:6379> zrem zset-key member1
(integer) 0
127.0.0.1:6379> zrange zset-key 0 -1 withscores
1) "member0"
2) "982"
```

## Redis 数据类型应用

### 案例-最受欢迎文章

选出最受欢迎文章，需要支持对文章进行评分。

#### 对文章进行投票

（1）使用 HASH 存储文章

使用 `HASH` 类型存储文章信息。其中：key 是文章 ID；field 是文章的属性 key；value 是属性对应值。

![img](https://raw.githubusercontent.com/dunwu/images/master/snap/20200225143038.jpg)

操作：

- 存储文章信息 - 使用 `HSET` 或 `HMGET` 命令
- 查询文章信息 - 使用 `HGETALL` 命令
- 添加投票 - 使用 `HINCRBY` 命令

（2）使用 `ZSET` 针对不同维度集合排序

使用 `ZSET` 类型分别存储按照时间排序和按照评分排序的文章 ID 集合。

![img](https://raw.githubusercontent.com/dunwu/images/master/snap/20200225145742.jpg)

操作：

- 添加记录 - 使用 `ZADD` 命令
- 添加分数 - 使用 `ZINCRBY` 命令
- 取出多篇文章 - 使用 `ZREVRANGE` 命令

（3）为了防止重复投票，使用 `SET` 类型记录每篇文章 ID 对应的投票集合。

![img](https://raw.githubusercontent.com/dunwu/images/master/snap/20200225150105.jpg)

操作：

- 添加投票者 - 使用 `SADD` 命令
- 设置有效期 - 使用 `EXPIRE` 命令

（4）假设 user:115423 给 article:100408 投票，分别需要高更新评分排序集合以及投票集合。

![img](https://raw.githubusercontent.com/dunwu/images/master/snap/20200225150138.jpg)

当需要对一篇文章投票时，程序需要用 ZSCORE 命令检查记录文章发布时间的有序集合，判断文章的发布时间是否超过投票有效期（比如：一星期）。

```java
    public void articleVote(Jedis conn, String user, String article) {
        // 计算文章的投票截止时间。
        long cutoff = (System.currentTimeMillis() / 1000) - ONE_WEEK_IN_SECONDS;

        // 检查是否还可以对文章进行投票
        // （虽然使用散列也可以获取文章的发布时间，
        // 但有序集合返回的文章发布时间为浮点数，
        // 可以不进行转换直接使用）。
        if (conn.zscore("time:", article) < cutoff) {
            return;
        }

        // 从article:id标识符（identifier）里面取出文章的ID。
        String articleId = article.substring(article.indexOf(':') + 1);

        // 如果用户是第一次为这篇文章投票，那么增加这篇文章的投票数量和评分。
        if (conn.sadd("voted:" + articleId, user) == 1) {
            conn.zincrby("score:", VOTE_SCORE, article);
            conn.hincrBy(article, "votes", 1);
        }
    }
```

#### 发布并获取文章

发布文章：

- 添加文章 - 使用 `INCR` 命令计算新的文章 ID，填充文章信息，然后用 `HSET` 命令或 `HMSET` 命令写入到 `HASH` 结构中。
- 将文章作者 ID 添加到投票名单 - 使用 `SADD` 命令添加到代表投票名单的 `SET` 结构中。
- 设置投票有效期 - 使用 `EXPIRE` 命令设置投票有效期。

```java
    public String postArticle(Jedis conn, String user, String title, String link) {
        // 生成一个新的文章ID。
        String articleId = String.valueOf(conn.incr("article:"));

        String voted = "voted:" + articleId;
        // 将发布文章的用户添加到文章的已投票用户名单里面，
        conn.sadd(voted, user);
        // 然后将这个名单的过期时间设置为一周（第3章将对过期时间作更详细的介绍）。
        conn.expire(voted, ONE_WEEK_IN_SECONDS);

        long now = System.currentTimeMillis() / 1000;
        String article = "article:" + articleId;
        // 将文章信息存储到一个散列里面。
        HashMap<String, String> articleData = new HashMap<String, String>();
        articleData.put("title", title);
        articleData.put("link", link);
        articleData.put("user", user);
        articleData.put("now", String.valueOf(now));
        articleData.put("votes", "1");
        conn.hmset(article, articleData);

        // 将文章添加到根据发布时间排序的有序集合和根据评分排序的有序集合里面。
        conn.zadd("score:", now + VOTE_SCORE, article);
        conn.zadd("time:", now, article);

        return articleId;
    }
```

分页查询最受欢迎文章：

使用 `ZINTERSTORE` 命令根据页码、每页记录数、排序号，根据评分值从大到小分页查出文章 ID 列表。

```java
    public List<Map<String, String>> getArticles(Jedis conn, int page, String order) {
        // 设置获取文章的起始索引和结束索引。
        int start = (page - 1) * ARTICLES_PER_PAGE;
        int end = start + ARTICLES_PER_PAGE - 1;

        // 获取多个文章ID。
        Set<String> ids = conn.zrevrange(order, start, end);
        List<Map<String, String>> articles = new ArrayList<>();
        // 根据文章ID获取文章的详细信息。
        for (String id : ids) {
            Map<String, String> articleData = conn.hgetAll(id);
            articleData.put("id", id);
            articles.add(articleData);
        }

        return articles;
    }
```

#### 对文章进行分组

如果文章需要分组，功能需要分为两块：

- 记录文章属于哪个群组
- 负责取出群组里的文章

将文章添加、删除群组：

```java
    public void addRemoveGroups(Jedis conn, String articleId, String[] toAdd, String[] toRemove) {
        // 构建存储文章信息的键名。
        String article = "article:" + articleId;
        // 将文章添加到它所属的群组里面。
        for (String group : toAdd) {
            conn.sadd("group:" + group, article);
        }
        // 从群组里面移除文章。
        for (String group : toRemove) {
            conn.srem("group:" + group, article);
        }
    }
```

取出群组里的文章：

![img](https://raw.githubusercontent.com/dunwu/images/master/snap/20200225214210.jpg)

- 通过对存储群组文章的集合和存储文章评分的有序集合执行 `ZINTERSTORE` 命令，可以得到按照文章评分排序的群组文章。
- 通过对存储群组文章的集合和存储文章发布时间的有序集合执行 `ZINTERSTORE` 命令，可以得到按照文章发布时间排序的群组文章。

```java
    public List<Map<String, String>> getGroupArticles(Jedis conn, String group, int page, String order) {
        // 为每个群组的每种排列顺序都创建一个键。
        String key = order + group;
        // 检查是否有已缓存的排序结果，如果没有的话就现在进行排序。
        if (!conn.exists(key)) {
            // 根据评分或者发布时间，对群组文章进行排序。
            ZParams params = new ZParams().aggregate(ZParams.Aggregate.MAX);
            conn.zinterstore(key, params, "group:" + group, order);
            // 让Redis在60秒钟之后自动删除这个有序集合。
            conn.expire(key, 60);
        }
        // 调用之前定义的getArticles函数来进行分页并获取文章数据。
        return getArticles(conn, page, key);
    }
```

### 案例-管理令牌

网站一般会以 Cookie、Session、令牌这类信息存储用户身份信息。

可以将 Cookie/Session/令牌 和用户的映射关系存储在 `HASH` 结构。

下面以令牌来举例。

#### 查询令牌

```java
    public String checkToken(Jedis conn, String token) {
        // 尝试获取并返回令牌对应的用户。
        return conn.hget("login:", token);
    }
```

#### 更新令牌

- 用户每次访问页面，可以记录下令牌和当前时间戳的映射关系，存入一个 `ZSET` 结构中，以便分析用户是否活跃，继而可以周期性清理最老的令牌，统计当前在线用户数等行为。
- 用户如果正在浏览商品，可以记录到用户最近浏览过的商品有序集合中（集合可以限定数量，超过数量进行裁剪），存入到一个 `ZSET` 结构中，以便分析用户最近可能感兴趣的商品，以便推荐商品。

```java
    public void updateToken(Jedis conn, String token, String user, String item) {
        // 获取当前时间戳。
        long timestamp = System.currentTimeMillis() / 1000;
        // 维持令牌与已登录用户之间的映射。
        conn.hset("login:", token, user);
        // 记录令牌最后一次出现的时间。
        conn.zadd("recent:", timestamp, token);
        if (item != null) {
            // 记录用户浏览过的商品。
            conn.zadd("viewed:" + token, timestamp, item);
            // 移除旧的记录，只保留用户最近浏览过的25个商品。
            conn.zremrangeByRank("viewed:" + token, 0, -26);
            conn.zincrby("viewed:", -1, item);
        }
    }
```

#### 清理令牌

上一节提到，更新令牌时，将令牌和当前时间戳的映射关系，存入一个 `ZSET` 结构中。所以可以通过排序得知哪些令牌最老。如果没有清理操作，更新令牌占用的内存会不断膨胀，直到导致机器宕机。

比如：最多允许存储 1000 万条令牌信息，周期性检查，一旦发现记录数超出 1000 万条，将 ZSET 从新到老排序，将超出 1000 万条的记录清除。

```java
    while (!quit) {
        // 找出目前已有令牌的数量。
        long size = conn.zcard("recent:");
        // 令牌数量未超过限制，休眠并在之后重新检查。
        if (size <= limit) {
            try {
                sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            continue;
        }

        // 获取需要移除的令牌ID。
        long endIndex = Math.min(size - limit, 100);
        Set<String> tokenSet = conn.zrange("recent:", 0, endIndex - 1);
        String[] tokens = tokenSet.toArray(new String[tokenSet.size()]);

        // 为那些将要被删除的令牌构建键名。
        ArrayList<String> sessionKeys = new ArrayList<String>();
        for (String token : tokens) {
            sessionKeys.add("viewed:" + token);
        }

        // 移除最旧的那些令牌。
        conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
        conn.hdel("login:", tokens);
        conn.zrem("recent:", tokens);
    }
```

### 案例-购物车

可以使用 HASH 结构来实现购物车功能。

每个用户的购物车，存储了商品 ID 和商品数量的映射。

#### 在购物车中添加、删除商品

```java
    public void addToCart(Jedis conn, String session, String item, int count) {
        if (count <= 0) {
            // 从购物车里面移除指定的商品。
            conn.hdel("cart:" + session, item);
        } else {
            // 将指定的商品添加到购物车。
            conn.hset("cart:" + session, item, String.valueOf(count));
        }
    }
```

#### 清空购物车

在 [清理令牌](#清理令牌) 的基础上，清空会话时，顺便将购物车缓存一并清理。

```java
   while (!quit) {
        long size = conn.zcard("recent:");
        if (size <= limit) {
            try {
                sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            continue;
        }

        long endIndex = Math.min(size - limit, 100);
        Set<String> sessionSet = conn.zrange("recent:", 0, endIndex - 1);
        String[] sessions = sessionSet.toArray(new String[sessionSet.size()]);

        ArrayList<String> sessionKeys = new ArrayList<String>();
        for (String sess : sessions) {
            sessionKeys.add("viewed:" + sess);
            // 新增加的这行代码用于删除旧会话对应用户的购物车。
            sessionKeys.add("cart:" + sess);
        }

        conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
        conn.hdel("login:", sessions);
        conn.zrem("recent:", sessions);
    }
```

### 案例-页面缓存

```
SETEX page_key context 300
```

### 案例-自动补全

需求：根据用户输入，自动补全信息，如：联系人、商品名等。

- 典型场景一：社交网站后台记录用户最近联系过的 100 个好友，当用户查找好友时，根据输入的关键字自动补全姓名。
- 典型场景二：电商网站后台记录用户最近浏览过的 10 件商品，当用户查找商品是，根据输入的关键字自动补全商品名称。

数据模型：使用 Redis 的 LIST 类型存储最近联系人列表。

构建自动补全列表通常有以下操作：

- 如果指定联系人已经存在于最近联系人列表里，那么从列表里移除他。对应 `LREM` 命令。
- 将指定联系人添加到最近联系人列表的最前面。对应 `LPUSH` 命令。
- 添加操作完成后，如果联系人列表中的数量超过 100 个，进行裁剪操作。对应 `LTRIM` 命令。

### 案例-职位搜索

需求：在一个招聘网站上，求职者有自己的技能清单；用人公司的职位有必要的技能清单。用人公司需要查询满足自己职位要求的求职者；求职者需要查询自己可以投递简历的职位。

关键数据模型：使用 `SET` 类型存储求职者的技能列表，使用 `SET` 类型存储职位的技能列表。

关键操作：使用 `SDIFF` 命令对比两个 `SET` 的差异，返回 `empty` 表示匹配要求。

redis cli 示例：

```shell
# -----------------------------------------------------------
# Redis 职位搜索数据模型示例
# -----------------------------------------------------------

# （1）职位技能表：使用 set 存储
# job:001 职位添加 4 种技能
SADD job:001 skill:001
SADD job:001 skill:002
SADD job:001 skill:003
SADD job:001 skill:004

# job:002 职位添加 3 种技能
SADD job:002 skill:001
SADD job:002 skill:002
SADD job:002 skill:003

# job:003 职位添加 2 种技能
SADD job:003 skill:001
SADD job:003 skill:003

# 查看
SMEMBERS job:001
SMEMBERS job:002
SMEMBERS job:003

# （2）求职者技能表：使用 set 存储
SADD interviewee:001 skill:001
SADD interviewee:001 skill:003

SADD interviewee:002 skill:001
SADD interviewee:002 skill:002
SADD interviewee:002 skill:003
SADD interviewee:002 skill:004
SADD interviewee:002 skill:005

# 查看
SMEMBERS interviewee:001
SMEMBERS interviewee:002

# （3）求职者遍历查找自己符合要求的职位（返回结果为 empty 表示要求的技能全部命中）
# 比较职位技能清单和求职者技能清单的差异
SDIFF job:001 interviewee:001
SDIFF job:002 interviewee:001
SDIFF job:003 interviewee:001

SDIFF job:001 interviewee:002
SDIFF job:002 interviewee:002
SDIFF job:003 interviewee:002

# （4）用人公司根据遍历查找符合自己职位要求的求职者（返回结果为 empty 表示要求的技能全部命中）
# 比较职位技能清单和求职者技能清单的差异
SDIFF interviewee:001 job:001
SDIFF interviewee:002 job:001

SDIFF interviewee:001 job:002
SDIFF interviewee:002 job:002

SDIFF interviewee:001 job:003
SDIFF interviewee:002 job:003
```

## 参考资料

- [《Redis 实战》](https://item.jd.com/11791607.html)
