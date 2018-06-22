# Redis 配置

## 通过配置文件配置

Redis 的配置文件名一般叫做：`redis.conf`。

redis.conf 文件中的配置指令参数格式为：

```
keyword argument1 argument2 ... argumentN
关键字   参数1     参数2      ... 参数N
```

## 通过命令行配置

自 Redis2.6 起就可以直接通过命令行传递 Redis 配置参数。这种方法可以用于测试。

例：这个例子配置一个新运行并以 6380 为端口的 Redis 实例，使配置它为 127.0.0.1:6379 Redis 实例的 slave。

```
./redis-server --port 6380 --slaveof 127.0.0.1 6379
```

## 动态修改配置

Redis 允许在运行的过程中，在不重启服务器的情况下更改服务器配置，同时也支持 使用特殊的 [CONFIG SET](https://redis.io/commands/config-set) 和 [CONFIG GET](https://redis.io/commands/config-get) 命令用编程方式查询并设置配置。

并非所有的配置指令都支持这种使用方式，但是大部分是支持的。

## 配置 Redis 成为一个缓存

如果你想把 Redis 当做一个缓存来用，所有的 key 都有过期时间，那么你可以考虑 使用以下设置（假设最大内存使用量为 2M）：

```
maxmemory 2mb
maxmemory-policy allkeys-lru
```

以上设置并不需要我们的应用使用 EXPIRE(或相似的命令)命令去设置每个 key 的过期时间，因为 只要内存使用量到达 2M，Redis 就会使用类 LRU 算法自动删除某些 key。

相比使用额外内存空间存储多个键的过期时间，使用缓存设置是一种更加有效利用内存的方式。而且相比每个键固定的 过期时间，使用 LRU 也是一种更加推荐的方式，因为这样能使应用的热数据(更频繁使用的键) 在内存中停留时间更久。

当我们把 Redis 当成缓存来使用的时候，如果应用程序同时也需要把 Redis 当成存储系统来使用，那么强烈建议 使用两个 Redis 实例。一个是缓存，使用上述方法进行配置，另一个是存储，根据应用的持久化需求进行配置，并且 只存储那些不需要被缓存的数据。

## 资料

- https://redis.io/topics/config
