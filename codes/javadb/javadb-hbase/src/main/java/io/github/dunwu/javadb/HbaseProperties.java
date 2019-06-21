package io.github.dunwu.javadb;

import java.io.Serializable;

/**
 * Hbase 配置参数管理对象
 * @author Zhang Peng
 */
public class HbaseProperties implements Serializable {

    private static final long serialVersionUID = 2930639554689310736L;

    private String hbaseMaster;

    private String quorum;

    private String clientPort;

    private String znodeParent;

    private String maxThreads;

    private String coreThreads;

    private String columnFamily;

    private String hbaseExecutorsNum = "10";

    private String ipcPoolSize;

    public HbaseProperties() {
    }

    public HbaseProperties(String hbaseMaster, String quorum, String clientPort, String znodeParent, String maxThreads,
        String coreThreads, String columnFamily, String hbaseExecutorsNum, String ipcPoolSize) {
        this.hbaseMaster = hbaseMaster;
        this.quorum = quorum;
        this.clientPort = clientPort;
        this.znodeParent = znodeParent;
        this.maxThreads = maxThreads;
        this.coreThreads = coreThreads;
        this.columnFamily = columnFamily;
        this.hbaseExecutorsNum = hbaseExecutorsNum;
        this.ipcPoolSize = ipcPoolSize;
    }

    public String getHbaseMaster() {
        return hbaseMaster;
    }

    public void setHbaseMaster(String hbaseMaster) {
        this.hbaseMaster = hbaseMaster;
    }

    public String getQuorum() {
        return quorum;
    }

    public void setQuorum(String quorum) {
        this.quorum = quorum;
    }

    public String getClientPort() {
        return clientPort;
    }

    public void setClientPort(String clientPort) {
        this.clientPort = clientPort;
    }

    public String getZnodeParent() {
        return znodeParent;
    }

    public void setZnodeParent(String znodeParent) {
        this.znodeParent = znodeParent;
    }

    public String getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(String maxThreads) {
        this.maxThreads = maxThreads;
    }

    public String getCoreThreads() {
        return coreThreads;
    }

    public void setCoreThreads(String coreThreads) {
        this.coreThreads = coreThreads;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    public String getHbaseExecutorsNum() {
        return hbaseExecutorsNum;
    }

    public void setHbaseExecutorsNum(String hbaseExecutorsNum) {
        this.hbaseExecutorsNum = hbaseExecutorsNum;
    }

    public String getIpcPoolSize() {
        return ipcPoolSize;
    }

    public void setIpcPoolSize(String ipcPoolSize) {
        this.ipcPoolSize = ipcPoolSize;
    }

    @Override
    public String toString() {
        return "HbaseProperties{" + "quorum='" + quorum + '\'' + ", clientPort='" + clientPort + '\''
            + ", znodeParent='" + znodeParent + '\'' + ", maxThreads='" + maxThreads + '\'' + ", coreThreads='"
            + coreThreads + '\'' + ", columnFamily='" + columnFamily + '\'' + ", hbaseExecutorsNum='"
            + hbaseExecutorsNum + '\'' + '}';
    }
}
