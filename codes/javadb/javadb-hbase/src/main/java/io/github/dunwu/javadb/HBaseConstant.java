package io.github.dunwu.javadb;

public enum HBaseConstant {
    HBASE_ZOOKEEPER_QUORUM("hbase.zookeeper.quorum"),
    HBASE_ENABLE("hbase.enable"),
    HBASE_MASTER("hbase.master"),
    HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT("hbase.zookeeper.property.clientPort"),
    HBASE_HCONNECTION_THREADS_MAX("hbase.hconnection.threads.max"),
    HBASE_HCONNECTION_THREADS_CORE("hbase.hconnection.threads.core"),
    ZOOKEEPER_ZNODE_PARENT("zookeeper.znode.parent"),
    HBASE_COLUMN_FAMILY("hbase.column.family"),
    HBASE_EXECUTOR_NUM("hbase.executor.num"),
    HBASE_IPC_POOL_SIZE("hbase.client.ipc.pool.size");

    private String key;

    HBaseConstant(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
