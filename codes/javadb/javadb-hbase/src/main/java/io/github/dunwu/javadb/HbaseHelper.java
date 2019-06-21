package io.github.dunwu.javadb;

import io.github.dunwu.util.base.PropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * HBase 服务实现类
 *
 * @author Zhang Peng
 * @date 2019-03-01
 */
public class HbaseHelper {

    private HbaseProperties hbaseProperties;

    private Connection connection;

    private static final String FIRST_CONFIG = "classpath://config//hbase.properties";

    private static final String SECOND_CONFIG = "classpath://application.properties";

    public HbaseHelper() throws Exception {
        // 初始化参数
        Properties properties = loadConfigFile();
        if (properties == null) {
            throw new Exception("读取 Hbase 配置失败，无法建立连接");
        }
        Boolean enable = PropertiesUtil.getBoolean(properties, HBaseConstant.HBASE_ENABLE.key(), true);
        if (!enable) {
            return;
        }
        String quorum = PropertiesUtil.getString(properties, HBaseConstant.HBASE_ZOOKEEPER_QUORUM.key(), "");
        String hbaseMaster = PropertiesUtil.getString(properties, HBaseConstant.HBASE_MASTER.key(), "");
        String clientPort = PropertiesUtil.getString(properties,
                                                     HBaseConstant.HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT.key(), "");
        String znodeParent = PropertiesUtil.getString(properties, HBaseConstant.ZOOKEEPER_ZNODE_PARENT.key(), "");
        String maxThreads = PropertiesUtil.getString(properties, HBaseConstant.HBASE_HCONNECTION_THREADS_MAX.key(), "");
        String coreThreads = PropertiesUtil.getString(properties, HBaseConstant.HBASE_HCONNECTION_THREADS_CORE.key(),
                                                      "");
        String columnFamily = PropertiesUtil.getString(properties, HBaseConstant.HBASE_COLUMN_FAMILY.key(), "");
        String hbaseExecutorsNum = PropertiesUtil.getString(properties, HBaseConstant.HBASE_EXECUTOR_NUM.key(), "10");
        String ipcPoolSize = PropertiesUtil.getString(properties, HBaseConstant.HBASE_IPC_POOL_SIZE.key(), "1");

        hbaseProperties = new HbaseProperties(hbaseMaster, quorum, clientPort, znodeParent, maxThreads, coreThreads,
                                              columnFamily, hbaseExecutorsNum, ipcPoolSize);
        init(hbaseProperties);
    }

    private Properties loadConfigFile() {
        Properties properties = null;
        try {
            properties = PropertiesUtil.loadFromFile(FIRST_CONFIG);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (properties == null) {
            try {
                properties = PropertiesUtil.loadFromFile(SECOND_CONFIG);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return properties;
    }

    public HbaseHelper(HbaseProperties hbaseProperties) throws Exception {
        this.hbaseProperties = hbaseProperties;
        init(hbaseProperties);
    }

    private void init(HbaseProperties hbaseProperties) throws Exception {
        try {
            // @formatter:off
            Configuration configuration = HBaseConfiguration.create();
            configuration.set(HBaseConstant.HBASE_ZOOKEEPER_QUORUM.key(), hbaseProperties.getQuorum());
            configuration.set(HBaseConstant.HBASE_MASTER.key(), hbaseProperties.getHbaseMaster());
            configuration.set(HBaseConstant.HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT.key(),
                    hbaseProperties.getClientPort());
            configuration.set(HBaseConstant.HBASE_HCONNECTION_THREADS_MAX.key(),
                    hbaseProperties.getMaxThreads());
            configuration.set(HBaseConstant.HBASE_HCONNECTION_THREADS_CORE.key(),
                    hbaseProperties.getCoreThreads());
            configuration.set(HBaseConstant.ZOOKEEPER_ZNODE_PARENT.key(), hbaseProperties.getZnodeParent());
            configuration.set(HBaseConstant.HBASE_COLUMN_FAMILY.key(), hbaseProperties.getColumnFamily());
            configuration.set(HBaseConstant.HBASE_IPC_POOL_SIZE.key(), hbaseProperties.getIpcPoolSize());
            // @formatter:on
            connection = ConnectionFactory.createConnection(configuration);
        } catch (Exception e) {
            throw new Exception("hbase链接未创建", e);
        }
    }

    public void destory() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public HTableDescriptor[] listTables() throws Exception {
        return listTables(null);
    }

    public HTableDescriptor[] listTables(String tableName) throws Exception {
        if (connection == null) {
            throw new Exception("hbase链接未创建");
        }

        HTableDescriptor[] hTableDescriptors = new HTableDescriptor[0];
        try {
            if (StringUtils.isEmpty(tableName)) {
                hTableDescriptors = connection.getAdmin()
                                              .listTables();
            } else {
                hTableDescriptors = connection.getAdmin()
                                              .listTables(tableName);
            }
        } catch (IOException e) {
            throw new Exception("执行失败", e);
        }
        return hTableDescriptors;
    }

    /**
     * 创建表
     * <p>
     * 等价于：
     * <ul>
     * <li>create 'tablename','family1','family2','family3'...</li>
     * </ul>
     */
    public void createTable(String tableName) throws Exception {
        createTable(tableName, new String[] {hbaseProperties.getColumnFamily()});
    }

    /**
     * 创建表
     * <p>
     * 等价于：
     * <ul>
     * <li>create 'tablename','family1','family2','family3'...</li>
     * </ul>
     */
    public void createTable(String tableName, String[] colFamilies) throws Exception {
        if (connection == null) {
            throw new Exception("hbase链接未创建");
        }

        try {
            TableName tablename = TableName.valueOf(tableName);
            // 如果表存在，先删除
            if (connection.getAdmin()
                          .isTableAvailable(tablename)) {
                dropTable(tableName);
            }
            HTableDescriptor tableDescriptor = new HTableDescriptor(tablename);
            for (String famliy : colFamilies) {
                tableDescriptor.addFamily(new HColumnDescriptor(famliy));
            }

            connection.getAdmin()
                      .createTable(tableDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除表
     * <p>
     * 等价于：
     * <ul>
     * <li>disable 'tablename'</li>
     * <li>drop 't1'</li>
     * </ul>
     *
     * @param name
     */
    public void dropTable(String name) throws Exception {
        if (connection == null) {
            throw new Exception("hbase链接未创建");
        }

        Admin admin = null;
        try {
            admin = connection.getAdmin();
            TableName tableName = TableName.valueOf(name);
            // 如果表存在，先删除
            if (admin.isTableAvailable(tableName)) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Put toPut(HbaseCellEntity hBaseTableDTO) throws Exception {
        if (connection == null) {
            throw new Exception("hbase链接未创建");
        }

        Put put = new Put(Bytes.toBytes(hBaseTableDTO.getRow()));
        put.addColumn(Bytes.toBytes(hBaseTableDTO.getColFamily()), Bytes.toBytes(hBaseTableDTO.getCol()),
                      Bytes.toBytes(hBaseTableDTO.getVal()));
        return put;
    }

    public void delete(String tableName, String rowKey) throws Exception {
        if (connection == null) {
            throw new Exception("hbase链接未创建");
        }

        Table table = null;
        try {
            table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("delete失败");
        }
    }

    public String resultToString(Result result) {
        if (result == null) {
            return null;
        }
        Cell[] cells = result.rawCells();
        StringBuilder sb = new StringBuilder();
        for (Cell cell : cells) {
            sb.append("{ ");
            sb.append("RowName -> ")
              .append(new String(CellUtil.cloneRow(cell)));
            sb.append(", Timetamp -> ")
              .append(cell.getTimestamp());
            sb.append(", Column Family -> ")
              .append(new String(CellUtil.cloneFamily(cell)));
            sb.append(", Row Name -> ")
              .append(new String(CellUtil.cloneQualifier(cell)));
            sb.append(", value -> ")
              .append(new String(CellUtil.cloneValue(cell)));
            sb.append(" }\n");
        }
        return sb.toString();
    }

    public Result get(String tableName, String rowKey) throws Exception {
        return get(tableName, rowKey, null, null);
    }

    public Result get(String tableName, String rowKey, String colFamily, String qualifier) throws Exception {
        if (connection == null) {
            throw new Exception("hbase链接未创建");
        }

        if (connection.isClosed()) {
            throw new Exception("hbase 连接已关闭");
        }

        if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(rowKey)) {
            return null;
        }

        Result result = null;
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));
            if (StringUtils.isNotEmpty(colFamily)) {
                if (StringUtils.isNotEmpty(qualifier)) {
                    get.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier));
                } else {
                    get.addFamily(Bytes.toBytes(colFamily));
                }
            }
            result = table.get(get);
        } catch (IOException e) {
            throw new Exception("查询时发生异常");
        }
        return result;
    }

    public Result get(String tableName, String rowKey, String colFamily) throws Exception {
        return get(tableName, rowKey, colFamily, null);
    }

    public Result[] scan(String tableName) throws Exception {
        return scan(tableName, null, null, null, null);
    }

    public Result[] scan(String tableName, String colFamily, String qualifier, String startRow, String stopRow)
        throws Exception {
        if (connection == null) {
            throw new Exception("hbase链接未创建");
        }

        if (StringUtils.isEmpty(tableName)) {
            return null;
        }

        ResultScanner resultScanner = null;
        List<Result> list = new ArrayList<>();
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            if (StringUtils.isNotEmpty(colFamily)) {
                if (StringUtils.isNotEmpty(qualifier)) {
                    scan.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier));
                }
                scan.addFamily(Bytes.toBytes(colFamily));
            }
            if (StringUtils.isNotEmpty(startRow)) {
                scan.setStartRow(Bytes.toBytes(startRow));
            }
            if (StringUtils.isNotEmpty(stopRow)) {
                scan.setStopRow(Bytes.toBytes(stopRow));
            }
            resultScanner = table.getScanner(scan);
            Result result = resultScanner.next();
            while (result != null) {
                list.add(result);
                result = resultScanner.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (resultScanner != null) {
                resultScanner.close();
            }
        }
        return list.toArray(new Result[0]);
    }

    public Result[] scan(String tableName, String colFamily) throws Exception {
        return scan(tableName, colFamily, null, null, null);
    }

    public Result[] scan(String tableName, String colFamily, String qualifier) throws Exception {
        return scan(tableName, colFamily, qualifier, null, null);
    }

    private List<Result> resultScannerToResults(ResultScanner resultScanner) {
        if (resultScanner == null) {
            return null;
        }

        List<Result> list = new ArrayList<>();
        Result result = null;
        try {
            result = resultScanner.next();
            while (result != null) {
                list.add(result);
                result = resultScanner.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public HbaseProperties getHbaseProperties() {
        return hbaseProperties;
    }

}
