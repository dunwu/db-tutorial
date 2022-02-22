# Elasticsearch 运维

> [Elasticsearch](https://github.com/elastic/elasticsearch) 是一个分布式、RESTful 风格的搜索和数据分析引擎，能够解决不断涌现出的各种用例。 作为 Elastic Stack 的核心，它集中存储您的数据，帮助您发现意料之中以及意料之外的情况。

<!-- TOC depthFrom:2 depthTo:3 -->

- [1. Elasticsearch 安装](#1-elasticsearch-安装)
- [2. Elasticsearch 集群规划](#2-elasticsearch-集群规划)
- [3. Elasticsearch 配置](#3-elasticsearch-配置)
- [4. Elasticsearch FAQ](#4-elasticsearch-faq)
  - [4.1. elasticsearch 不允许以 root 权限来运行](#41-elasticsearch-不允许以-root-权限来运行)
  - [4.2. vm.max_map_count 不低于 262144](#42-vmmax_map_count-不低于-262144)
  - [4.3. nofile 不低于 65536](#43-nofile-不低于-65536)
  - [4.4. nproc 不低于 2048](#44-nproc-不低于-2048)
- [5. 参考资料](#5-参考资料)

<!-- /TOC -->

## 1. Elasticsearch 安装

> [Elasticsearch 官方下载安装说明](https://www.elastic.co/cn/downloads/elasticsearch)

（1）下载解压

访问 [官方下载地址](https://www.elastic.co/cn/downloads/elasticsearch) ，选择需要的版本，下载解压到本地。

（2）运行

运行 `bin/elasticsearch` (Windows 系统上运行 `bin\elasticsearch.bat` )

（3）访问

执行 `curl http://localhost:9200/` 测试服务是否启动

## 2. Elasticsearch 集群规划

ElasticSearch 集群需要根据业务实际情况去合理规划。

需要考虑的问题点：

- 集群部署几个节点？
- 有多少个索引？
- 每个索引有多大数据量？
- 每个索引有多少个分片？

一个参考规划：

- 3 台机器，每台机器是 6 核 64G 的。
- 我们 es 集群的日增量数据大概是 2000 万条，每天日增量数据大概是 500MB，每月增量数据大概是 6 亿，15G。目前系统已经运行了几个月，现在 es 集群里数据总量大概是 100G 左右。
- 目前线上有 5 个索引（这个结合你们自己业务来，看看自己有哪些数据可以放 es 的），每个索引的数据量大概是 20G，所以这个数据量之内，我们每个索引分配的是 8 个 shard，比默认的 5 个 shard 多了 3 个 shard。

## 3. Elasticsearch 配置

ES 的默认配置文件为 `config/elasticsearch.yml`

基本配置说明如下：

```yml
cluster.name: elasticsearch
#配置es的集群名称，默认是elasticsearch，es会自动发现在同一网段下的es，如果在同一网段下有多个集群，就可以用这个属性来区分不同的集群。
node.name: 'Franz Kafka'
#节点名，默认随机指定一个name列表中名字，该列表在es的jar包中config文件夹里name.txt文件中，其中有很多作者添加的有趣名字。
node.master: true
#指定该节点是否有资格被选举成为node，默认是true，es是默认集群中的第一台机器为master，如果这台机挂了就会重新选举master。
node.data: true
#指定该节点是否存储索引数据，默认为true。
index.number_of_shards: 5
#设置默认索引分片个数，默认为5片。
index.number_of_replicas: 1
#设置默认索引副本个数，默认为1个副本。
path.conf: /path/to/conf
#设置配置文件的存储路径，默认是es根目录下的config文件夹。
path.data: /path/to/data
#设置索引数据的存储路径，默认是es根目录下的data文件夹，可以设置多个存储路径，用逗号隔开，例：
#path.data: /path/to/data1,/path/to/data2
path.work: /path/to/work
#设置临时文件的存储路径，默认是es根目录下的work文件夹。
path.logs: /path/to/logs
#设置日志文件的存储路径，默认是es根目录下的logs文件夹
path.plugins: /path/to/plugins
#设置插件的存放路径，默认是es根目录下的plugins文件夹
bootstrap.mlockall: true
#设置为true来锁住内存。因为当jvm开始swapping时es的效率会降低，所以要保证它不swap，可以把#ES_MIN_MEM和ES_MAX_MEM两个环境变量设置成同一个值，并且保证机器有足够的内存分配给es。同时也要#允许elasticsearch的进程可以锁住内存，linux下可以通过`ulimit -l unlimited`命令。
network.bind_host: 192.168.0.1
#设置绑定的ip地址，可以是ipv4或ipv6的，默认为0.0.0.0。
network.publish_host: 192.168.0.1
#设置其它节点和该节点交互的ip地址，如果不设置它会自动判断，值必须是个真实的ip地址。
network.host: 192.168.0.1
#这个参数是用来同时设置bind_host和publish_host上面两个参数。
transport.tcp.port: 9300
#设置节点间交互的tcp端口，默认是9300。
transport.tcp.compress: true
#设置是否压缩tcp传输时的数据，默认为false，不压缩。
http.port: 9200
#设置对外服务的http端口，默认为9200。
http.max_content_length: 100mb
#设置内容的最大容量，默认100mb
http.enabled: false
#是否使用http协议对外提供服务，默认为true，开启。
gateway.type: local
#gateway的类型，默认为local即为本地文件系统，可以设置为本地文件系统，分布式文件系统，hadoop的#HDFS，和amazon的s3服务器，其它文件系统的设置方法下次再详细说。
gateway.recover_after_nodes: 1
#设置集群中N个节点启动时进行数据恢复，默认为1。
gateway.recover_after_time: 5m
#设置初始化数据恢复进程的超时时间，默认是5分钟。
gateway.expected_nodes: 2
#设置这个集群中节点的数量，默认为2，一旦这N个节点启动，就会立即进行数据恢复。
cluster.routing.allocation.node_initial_primaries_recoveries: 4
#初始化数据恢复时，并发恢复线程的个数，默认为4。
cluster.routing.allocation.node_concurrent_recoveries: 2
#添加删除节点或负载均衡时并发恢复线程的个数，默认为4。
indices.recovery.max_size_per_sec: 0
#设置数据恢复时限制的带宽，如入100mb，默认为0，即无限制。
indices.recovery.concurrent_streams: 5
#设置这个参数来限制从其它分片恢复数据时最大同时打开并发流的个数，默认为5。
discovery.zen.minimum_master_nodes: 1
#设置这个参数来保证集群中的节点可以知道其它N个有master资格的节点。默认为1，对于大的集群来说，可以设置大一点的值（2-4）
discovery.zen.ping.timeout: 3s
#设置集群中自动发现其它节点时ping连接超时时间，默认为3秒，对于比较差的网络环境可以高点的值来防止自动发现时出错。
discovery.zen.ping.multicast.enabled: false
#设置是否打开多播发现节点，默认是true。
discovery.zen.ping.unicast.hosts: ['host1', 'host2:port', 'host3[portX-portY]']
#设置集群中master节点的初始列表，可以通过这些节点来自动发现新加入集群的节点。
```

## 4. Elasticsearch FAQ

### 4.1. elasticsearch 不允许以 root 权限来运行

**问题：**在 Linux 环境中，elasticsearch 不允许以 root 权限来运行。

如果以 root 身份运行 elasticsearch，会提示这样的错误：

```
can not run elasticsearch as root
```

**解决方法：**使用非 root 权限账号运行 elasticsearch

```bash
# 创建用户组
groupadd elk
# 创建新用户，-g elk 设置其用户组为 elk，-p elk 设置其密码为 elk
useradd elk -g elk -p elk
# 更改 /opt 文件夹及内部文件的所属用户及组为 elk:elk
chown -R elk:elk /opt # 假设你的 elasticsearch 安装在 opt 目录下
# 切换账号
su elk
```

### 4.2. vm.max_map_count 不低于 262144

**问题：**`vm.max_map_count` 表示虚拟内存大小，它是一个内核参数。elasticsearch 默认要求 `vm.max_map_count` 不低于 262144。

```
max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
```

**解决方法：**

你可以执行以下命令，设置 `vm.max_map_count` ，但是重启后又会恢复为原值。

```
sysctl -w vm.max_map_count=262144
```

持久性的做法是在 `/etc/sysctl.conf` 文件中修改 `vm.max_map_count` 参数：

```
echo "vm.max_map_count=262144" > /etc/sysctl.conf
sysctl -p
```

> **注意**
>
> 如果运行环境为 docker 容器，可能会限制执行 sysctl 来修改内核参数。
>
> 这种情况下，你只能选择直接修改宿主机上的参数了。

### 4.3. nofile 不低于 65536

**问题：** `nofile` 表示进程允许打开的最大文件数。elasticsearch 进程要求可以打开的最大文件数不低于 65536。

```
max file descriptors [4096] for elasticsearch process is too low, increase to at least [65536]
```

**解决方法：**

在 `/etc/security/limits.conf` 文件中修改 `nofile` 参数：

```
echo "* soft nofile 65536" > /etc/security/limits.conf
echo "* hard nofile 131072" > /etc/security/limits.conf
```

### 4.4. nproc 不低于 2048

**问题：** `nproc` 表示最大线程数。elasticsearch 要求最大线程数不低于 2048。

```
max number of threads [1024] for user [user] is too low, increase to at least [2048]
```

**解决方法：**

在 `/etc/security/limits.conf` 文件中修改 `nproc` 参数：

```
echo "* soft nproc 2048" > /etc/security/limits.conf
echo "* hard nproc 4096" > /etc/security/limits.conf
```

## 5. 参考资料

- [Elasticsearch 官方下载安装说明](https://www.elastic.co/cn/downloads/elasticsearch)
- [Install Elasticsearch with RPM](https://www.elastic.co/guide/en/elasticsearch/reference/current/rpm.html#rpm)
- [Elasticsearch 使用积累](http://siye1982.github.io/2015/09/17/es-optimize/)
