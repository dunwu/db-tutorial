# Redis 发布与订阅

Redis 提供了 5 个发布与订阅命令：

| 命令           | 描述                                                                |
| -------------- | ------------------------------------------------------------------- |
| `SUBSCRIBE`    | `SUBSCRIBE channel [channel ...]`—订阅指定频道。                    |
| `UNSUBSCRIBE`  | `UNSUBSCRIBE [channel [channel ...]]`—取消订阅指定频道。            |
| `PUBLISH`      | `PUBLISH channel message`—发送信息到指定的频道。                    |
| `PSUBSCRIBE`   | `PSUBSCRIBE pattern [pattern ...]`—订阅符合指定模式的频道。         |
| `PUNSUBSCRIBE` | `PUNSUBSCRIBE [pattern [pattern ...]]`—取消订阅符合指定模式的频道。 |

## 参考资料

- **官网**
  - [Redis 官网](https://redis.io/)
  - [Redis github](https://github.com/antirez/redis)
  - [Redis 官方文档中文版](http://redis.cn/)
- **书籍**
  - [《Redis 实战》](https://item.jd.com/11791607.html)
  - [《Redis 设计与实现》](https://item.jd.com/11486101.html)
- **教程**
  - [Redis 命令参考](http://redisdoc.com/)
