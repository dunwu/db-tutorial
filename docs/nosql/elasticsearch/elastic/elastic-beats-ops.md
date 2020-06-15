# Filebeat 运维

> Beats 平台集合了多种单一用途数据采集器。它们从成百上千或成千上万台机器和系统向 Logstash 或 Elasticsearch 发送数据。
>
> 因为我只接触过 Filebeat，所有本文仅介绍 Filebeat 的日常运维。

## 1. Filebeat 安装

### 1.1. 环境要求

> 版本：Elastic Stack 7.4

### 1.2. 安装步骤

Unix / Linux 系统建议使用下面方式安装，因为比较通用。

```
wget https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-6.1.1-linux-x86_64.tar.gz
tar -zxf filebeat-6.1.1-linux-x86_64.tar.gz
```

> 更多内容可以参考：[filebeat-installation](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-installation.html)

## 2. Filebeat 配置

> 首先，必须要知道的是：`filebeat.yml` 是 filebeat 的配置文件。其路径会因为你安装方式而有所不同。
>
> Beat 所有系列产品的配置文件都基于 [YAML](http://www.yaml.org/) 格式，FileBeat 当然也不例外。
>
> 更多 filebeat 配置内容可以参考：[配置 filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/configuring-howto-filebeat.html)
>
> 更多 filebeat.yml 文件格式内容可以参考：[filebeat.yml 文件格式](https://www.elastic.co/guide/en/beats/libbeat/6.1/config-file-format.html)

filebeat.yml 部分配置示例：

```yml
filebeat:
  prospectors:
    - type: log
      paths:
        - /var/log/*.log
      multiline:
        pattern: '^['
        match: after
```

### 2.1. 重要配置项

> 下面我将列举 Filebeat 的较为重要的配置项。
>
> 如果想了解更多配置信息，可以参考：
>
> 更多 filebeat 配置内容可以参考：[配置 filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/configuring-howto-filebeat.html)
>
> 更多 filebeat.yml 文件格式内容可以参考：[filebeat.yml 文件格式](https://www.elastic.co/guide/en/beats/libbeat/6.1/config-file-format.html)

#### 2.1.1. filebeat.prospectors

（文件监视器）用于指定需要关注的文件。

**示例**

```yaml
filebeat.prospectors:
  - type: log
    enabled: true
    paths:
      - /var/log/*.log
```

#### 2.1.2. output.elasticsearch

如果你希望使用 filebeat 直接向 elasticsearch 输出数据，需要配置 output.elasticsearch 。

**示例**

```yaml
output.elasticsearch:
  hosts: ['192.168.1.42:9200']
```

#### 2.1.3. output.logstash

如果你希望使用 filebeat 向 logstash 输出数据，然后由 logstash 再向 elasticsearch 输出数据，需要配置 output.logstash。

> **注意**
>
> 相比于向 elasticsearch 输出数据，个人更推荐向 logstash 输出数据。
>
> 因为 logstash 和 filebeat 一起工作时，如果 logstash 忙于处理数据，会通知 FileBeat 放慢读取速度。一旦拥塞得到解决，FileBeat 将恢复到原来的速度并继续传播。这样，可以减少管道超负荷的情况。

**示例**

```yaml
output.logstash:
  hosts: ['127.0.0.1:5044']
```

此外，还需要在 logstash 的配置文件（如 logstash.conf）中指定 beats input 插件：

```yaml
input {
  beats {
    port => 5044 # 此端口需要与 filebeat.yml 中的端口相同
  }
}

# The filter part of this file is commented out to indicate that it is
# optional.
# filter {
#
# }

output {
  elasticsearch {
    hosts => "localhost:9200"
    manage_template => false
    index => "%{[@metadata][beat]}-%{[@metadata][version]}-%{+YYYY.MM.dd}"
    document_type => "%{[@metadata][type]}"
  }
}
```

#### 2.1.4. setup.kibana

如果打算使用 Filebeat 提供的 Kibana 仪表板，需要配置 setup.kibana 。

**示例**

```yaml
setup.kibana:
  host: 'localhost:5601'
```

#### 2.1.5. setup.template.settings

在 Elasticsearch 中，[索引模板](https://www.elastic.co/guide/en/elasticsearch/reference/6.1/indices-templates.html)用于定义设置和映射，以确定如何分析字段。

在 Filebeat 中，setup.template.settings 用于配置索引模板。

Filebeat 推荐的索引模板文件由 Filebeat 软件包安装。如果您接受 filebeat.yml 配置文件中的默认配置，Filebeat 在成功连接到 Elasticsearch 后自动加载模板。

您可以通过在 Filebeat 配置文件中配置模板加载选项来禁用自动模板加载，或加载自己的模板。您还可以设置选项来更改索引和索引模板的名称。

> **参考**
>
> 更多内容可以参考：[filebeat-template](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-template.html)
>
> **说明**
>
> 如无必要，使用 Filebeat 配置文件中的默认索引模板即可。

#### 2.1.6. setup.dashboards

Filebeat 附带了示例 Kibana 仪表板。在使用仪表板之前，您需要创建索引模式 `filebeat- *`，并将仪表板加载到 Kibana 中。为此，您可以运行 `setup` 命令或在 `filebeat.yml` 配置文件中配置仪表板加载。

为了在 Kibana 中加载 Filebeat 的仪表盘，需要在 `filebeat.yml` 配置中启动开关：

```
setup.dashboards.enabled: true
```

> **参考**
>
> 更多内容可以参考：[configuration-dashboards](https://www.elastic.co/guide/en/beats/filebeat/current/configuration-dashboards.html)

## 3. Filebeat 命令

filebeat 提供了一系列命令来完成各种功能。

执行命令方式：

```bash
./filebeat COMMAND
```

> **参考**
>
> 更多内容可以参考：[command-line-options](https://www.elastic.co/guide/en/beats/filebeat/current/command-line-options.html)
>
> **说明**
>
> 个人认为命令行没有必要一一掌握，因为绝大部分功能都可以通过配置来完成。且通过命令行指定功能这种方式要求每次输入同样参数，不利于固化启动方式。
>
> 最重要的当然是启动命令 run 了。
>
> **示例** 指定配置文件启动
>
> ```bash
> ./filebeat run -e -c filebeat.yml -d "publish"
> ./filebeat -e -c filebeat.yml -d "publish" # run 可以省略
> ```

## 4. Filebeat 模块

> [Filebeat](https://www.elastic.co/cn/products/beats/filebeat) 和 [Metricbeat](https://www.elastic.co/cn/products/beats/metricbeat) 内部集成了一系列模块，用以简化常见日志格式（例如 NGINX、Apache 或诸如 Redis 或 Docker 等系统指标）的收集、解析和可视化过程。

- 配置 elasticsearch 和 kibana

```
output.elasticsearch:
  hosts: ["myEShost:9200"]
  username: "elastic"
  password: "elastic"
setup.kibana:
  host: "mykibanahost:5601"
  username: "elastic"
  password: "elastic
```

> username 和 password 是可选的，如果不需要认证则不填。

- 初始化环境

执行下面命令，filebeat 会加载推荐索引模板。

```
./filebeat setup -e
```

- 指定模块

执行下面命令，指定希望加载的模块。

```
./filebeat -e --modules system,nginx,mysql
```

> 更多内容可以参考：
>
> - [配置 filebeat 模块](https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-modules.html)
> - [filebeat 支持模块](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-modules.html)

## 5. 参考资料

- [Beats 官网](https://www.elastic.co/cn/products/beats)
- [Beats Github](https://github.com/elastic/beats)
- [Beats 官方文档](https://www.elastic.co/guide/en/beats/libbeat/current/index.html)
