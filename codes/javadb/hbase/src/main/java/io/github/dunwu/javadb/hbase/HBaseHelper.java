package io.github.dunwu.javadb.hbase;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.hbase.entity.HBaseFamilyRequest;
import io.github.dunwu.javadb.hbase.entity.HBaseMultiFamilyRequest;
import io.github.dunwu.javadb.hbase.entity.HBaseRowData;
import io.github.dunwu.javadb.hbase.entity.PageData;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HBase CRUD 工具类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-03-27
 */
@Slf4j
public class HBaseHelper implements Closeable {

    private final Connection connection;
    private final Configuration configuration;

    protected HBaseHelper(Configuration configuration) throws IOException {
        this.configuration = configuration;
        this.connection = ConnectionFactory.createConnection(configuration);
    }

    protected HBaseHelper(Connection connection) {
        this.configuration = connection.getConfiguration();
        this.connection = connection;
    }

    public static synchronized HBaseHelper newInstance(Configuration configuration) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration can not be null!");
        }
        return new HBaseHelper(configuration);
    }

    public synchronized static HBaseHelper newInstance(Connection connection) throws IOException {
        if (connection == null) {
            throw new IllegalArgumentException("connection can not be null!");
        }
        return new HBaseHelper(connection);
    }

    /**
     * 关闭内部持有的 HBase Connection 实例
     */
    @Override
    public synchronized void close() {
        if (null == connection || connection.isClosed()) {
            return;
        }
        IoUtil.close(connection);
    }

    /**
     * 获取 HBase 连接实例
     *
     * @return /
     */
    public Connection getConnection() {
        if (null == connection) {
            throw new RuntimeException("HBase connection init failed...");
        }
        return connection;
    }

    /**
     * 获取 HBase 配置
     *
     * @return /
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 获取 {@link Table} 实例
     *
     * @param tableName 表名
     * @return /
     */
    public Table getTable(String tableName) throws Exception {
        return getTable(TableName.valueOf(tableName));
    }

    /**
     * 获取 {@link Table} 实例
     *
     * @param tableName 表名
     * @return /
     */

    public synchronized Table getTable(TableName tableName) throws Exception {
        return connection.getTable(tableName);
    }

    public void put(TableName tableName, String row, String family, String column, String value) throws Exception {
        put(tableName, row, family, null, column, value);
    }

    public void put(TableName tableName, String row, String family, Long timestamp, String column, String value)
        throws Exception {
        Table table = getTable(tableName);
        try {
            Put put = new Put(Bytes.toBytes(row));
            if (timestamp != null) {
                put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), timestamp, Bytes.toBytes(value));
            } else {
                put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
            }
            table.put(put);
        } finally {
            recycle(table);
        }
    }

    public void put(TableName tableName, String row, String family, Object obj) throws Exception {
        put(tableName, row, family, null, obj);
    }

    @SuppressWarnings("unchecked")
    public void put(TableName tableName, String row, String family, Long timestamp, Object obj) throws Exception {
        Map<String, Object> map;
        if (obj instanceof Map) {
            map = (Map<String, Object>) obj;
        } else {
            map = BeanUtil.beanToMap(obj);
        }
        put(tableName, row, family, timestamp, map);
    }

    public void put(TableName tableName, String row, String family, Map<String, Object> columnMap) throws Exception {
        put(tableName, row, family, null, columnMap);
    }

    public void put(TableName tableName, String row, String family, Long timestamp, Map<String, Object> columnMap)
        throws Exception {
        Put put = new Put(Bytes.toBytes(row));
        columnMap.forEach((column, value) -> {
            if (value != null) {
                if (timestamp != null) {
                    put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), timestamp,
                        Bytes.toBytes(String.valueOf(value)));
                } else {
                    put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(String.valueOf(value)));
                }
            }
        });
        Table table = getTable(tableName);
        try {
            table.put(put);
        } finally {
            recycle(table);
        }
    }

    public void put(TableName tableName, String row, Long timestamp, Map<String, Map<String, Object>> familyMap)
        throws Exception {
        Put put = new Put(Bytes.toBytes(row));
        for (Map.Entry<String, Map<String, Object>> e : familyMap.entrySet()) {
            String family = e.getKey();
            Map<String, Object> columnMap = e.getValue();
            if (MapUtil.isNotEmpty(columnMap)) {
                for (Map.Entry<String, Object> entry : columnMap.entrySet()) {
                    String column = entry.getKey();
                    Object value = entry.getValue();

                    if (ObjectUtil.isEmpty(value)) {
                        continue;
                    }

                    if (timestamp != null) {
                        put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), timestamp,
                            Bytes.toBytes(String.valueOf(value)));
                    } else {
                        put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column),
                            Bytes.toBytes(String.valueOf(value)));
                    }
                }
            }
        }
        Table table = getTable(tableName);
        try {
            table.put(put);
        } finally {
            recycle(table);
        }
    }

    public void deleteRow(TableName tableName, String row) throws Exception {
        Delete delete = new Delete(Bytes.toBytes(row));
        Table table = getTable(tableName);
        try {
            table.delete(delete);
        } finally {
            recycle(table);
        }
    }

    public long incrementColumnValue(TableName tableName, String row, String family, String column, long amount)
        throws Exception {
        return incrementColumnValue(tableName, row, family, column, amount, Durability.SYNC_WAL);
    }

    public long incrementColumnValue(TableName tableName, String row, String family, String column, long amount,
        Durability durability) throws Exception {
        Table table = getTable(tableName);
        try {
            return table.incrementColumnValue(Bytes.toBytes(row), Bytes.toBytes(family), Bytes.toBytes(column), amount,
                durability);
        } finally {
            recycle(table);
        }
    }

    public void dump(TableName tableName, String[] rows, String[] families, String[] columns) throws Exception {

        List<Get> gets = new ArrayList<>();
        for (String row : rows) {
            Get get = new Get(Bytes.toBytes(row));
            if (families != null) {
                for (String family : families) {
                    for (String column : columns) {
                        get.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
                    }
                }
            }
            gets.add(get);
        }

        Table table = getTable(tableName);
        try {
            Result[] results = table.get(gets);
            for (Result result : results) {
                for (Cell cell : result.rawCells()) {
                    System.out.println(
                        "Cell: " + cell + ", Value: " + Bytes.toString(cell.getValueArray(), cell.getValueOffset(),
                            cell.getValueLength()));
                }
            }
        } finally {
            recycle(table);
        }
    }

    public void dump(TableName tableName) throws Exception {
        Table table = getTable(tableName);
        try {
            ResultScanner scanner = table.getScanner(new Scan());
            for (Result result : scanner) {
                dumpResult(result);
            }
            scanner.close();
        } finally {
            recycle(table);
        }
    }

    /**
     * 指定行、列族、列，返回相应单元中的值
     *
     * @param tableName 表名
     * @param row       指定行
     * @param family    列族
     * @param column    列
     * @return /
     */
    public String getColumn(TableName tableName, String row, String family, String column) throws Exception {
        Get get = new Get(Bytes.toBytes(row));
        Table table = getTable(tableName);
        try {
            Result result = table.get(get);
            return Bytes.toString(result.getValue(Bytes.toBytes(family), Bytes.toBytes(column)));
        } finally {
            recycle(table);
        }
    }

    /**
     * 返回指定行、列族，列的数据，以实体 {@link T} 形式返回数据
     *
     * @param tableName 表名
     * @param row       指定行
     * @param family    列族
     * @param clazz     返回实体类型
     * @param <T>       实体类型
     * @return /
     */
    public <T> T getFamilyMap(TableName tableName, String row, String family, Class<T> clazz) throws Exception {
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(clazz);
        Set<String> columns = fieldMap.keySet();
        Map<String, String> map = getFamilyMap(tableName, row, family, columns);
        if (MapUtil.isEmpty(map)) {
            return null;
        }
        return BeanUtil.mapToBean(map, clazz, true, CopyOptions.create().ignoreError());
    }

    /**
     * 返回指定行、列族，列的数据，并以 {@link Map} 形式返回
     *
     * @param tableName 表名
     * @param row       指定行
     * @param family    列族
     * @param columns   指定列
     * @return /
     */
    public Map<String, String> getFamilyMap(TableName tableName, String row, String family,
        Collection<String> columns) throws Exception {

        if (CollectionUtil.isEmpty(columns)) {
            return getFamilyMap(tableName, row, family);
        }

        List<Get> gets = new ArrayList<>();
        Get get = new Get(Bytes.toBytes(row));
        for (String column : columns) {
            get.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
        }
        gets.add(get);

        Table table = getTable(tableName);
        try {
            Result[] results = table.get(gets);
            Map<String, String> map = new HashMap<>(columns.size());
            for (Result result : results) {
                for (String column : columns) {
                    String value = Bytes.toString(result.getValue(Bytes.toBytes(family), Bytes.toBytes(column)));
                    map.put(column, value);
                }
            }
            return map;
        } finally {
            recycle(table);
        }
    }

    /**
     * 返回指定行、列族的所有列数据，并以 {@link Map} 形式返回
     *
     * @param tableName 表名
     * @param row       指定行
     * @param family    列族
     * @return /
     */
    public Map<String, String> getFamilyMap(TableName tableName, String row, String family) throws Exception {
        List<Get> gets = new ArrayList<>();
        Get get = new Get(Bytes.toBytes(row));
        gets.add(get);
        Table table = getTable(tableName);
        try {
            Result[] results = table.get(gets);
            Map<String, Map<String, String>> familyColumnMap = getAllFamilyMap(results, row);
            return familyColumnMap.get(family);
        } finally {
            recycle(table);
        }
    }

    /**
     * 指定多个row进行批量查询
     *
     * @param tableName
     * @param rows
     * @param family
     * @param columns
     * @return
     * @throws Exception
     */
    public Map<String, Map<String, String>> getFamilyMapInRows(TableName tableName, List<String> rows, String family,
        Collection<String> columns) throws Exception {

        List<Get> gets = new ArrayList<>();
        for (String row : rows) {
            Get get = new Get(Bytes.toBytes(row));
            for (String column : columns) {
                get.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            }
            gets.add(get);
        }
        Table table = getTable(tableName);
        try {
            Result[] results = table.get(gets);
            Map<String, Map<String, String>> resultMap = new LinkedHashMap<>(gets.size());
            for (Result result : results) {
                Map<String, String> map = new HashMap<>(columns.size());
                for (String column : columns) {
                    String value = Bytes.toString(result.getValue(Bytes.toBytes(family), Bytes.toBytes(column)));
                    map.put(column, value);
                }
                resultMap.put(Bytes.toString(result.getRow()), map);
            }
            return resultMap;
        } finally {
            recycle(table);
        }
    }

    /**
     * 返回指定行、列族，列的数据，并以 {@link Map} 形式返回
     *
     * @param tableName     表名
     * @param row           指定行
     * @param familyColumns <列族, 要查询的列>
     * @return /
     */
    public Map<String, Map<String, String>> getMultiFamilyMap(TableName tableName, String row,
        Map<String, Collection<String>> familyColumns) throws Exception {

        if (MapUtil.isEmpty(familyColumns)) {
            return getMultiFamilyMap(tableName, row);
        }

        List<Get> gets = new ArrayList<>();
        Get get = new Get(Bytes.toBytes(row));
        for (Map.Entry<String, Collection<String>> entry : familyColumns.entrySet()) {
            String family = entry.getKey();
            Collection<String> columns = entry.getValue();
            if (CollectionUtil.isNotEmpty(columns)) {
                for (String column : columns) {
                    get.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
                }
            }
        }
        gets.add(get);

        Table table = getTable(tableName);
        try {
            Result[] results = table.get(gets);
            Map<String, Map<String, String>> map = new HashMap<>(familyColumns.size());
            for (Result result : results) {
                if (result == null || result.isEmpty()) {
                    continue;
                }

                familyColumns.forEach((family, columns) -> {
                    Map<String, String> kvMap = new HashMap<>();
                    if (CollectionUtil.isNotEmpty(columns)) {
                        for (String column : columns) {
                            String value =
                                Bytes.toString(result.getValue(Bytes.toBytes(family), Bytes.toBytes(column)));
                            kvMap.put(column, value);
                        }
                    }
                    map.put(family, kvMap);
                });
            }
            return map;
        } finally {
            recycle(table);
        }
    }

    /**
     * 返回指定行所有列族的数据，并以 {@link Map} 形式返回
     *
     * @param tableName 表名
     * @param row       指定行
     * @return /
     */
    public Map<String, Map<String, String>> getMultiFamilyMap(TableName tableName, String row) throws Exception {

        List<Get> gets = new ArrayList<>();
        Get get = new Get(Bytes.toBytes(row));
        gets.add(get);

        Table table = getTable(tableName);
        try {
            Result[] results = table.get(gets);
            return getAllFamilyMap(results, row);
        } finally {
            recycle(table);
        }
    }

    private Map<String, Map<String, String>> getAllFamilyMap(Result[] results, String row) {
        Map<String, Map<String, Map<String, String>>> rowFamilyColumnMap = getAllFamilyMapInRows(results);
        if (MapUtil.isEmpty(rowFamilyColumnMap)) {
            return new HashMap<>(0);
        }
        return rowFamilyColumnMap.get(row);
    }

    public HBaseRowData getRowData(TableName tableName, String row, Map<String, Collection<String>> familyColumns)
        throws Exception {
        Map<String, Map<String, String>> familyColumnMap = getMultiFamilyMap(tableName, row, familyColumns);
        return HBaseRowData.buildByMap(row, null, familyColumnMap);
    }

    /**
     * 指定起止行、列族、多个列、{@link Filter}，进行范围查询
     *
     * @param tableName 表
     * @param startRow  起始行
     * @param stopRow   结束行
     * @param stopRow   rowInclude   控制范围闭合条件[startRowInclude, endRowInclude],默认左闭右开
     * @param family    列族
     * @param columns   将要返回的列（未指定的列不会返回）
     * @param operator  filter执行条件
     * @param filters   {@link Filter} 实体
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, String>> scanFamilyMap(TableName tableName, String startRow, String stopRow,
        boolean[] rowInclude, String family, Collection<String> columns, FilterList.Operator operator,
        Filter... filters) throws Exception {
        Scan scan = new Scan();
        fillColumnsToScan(family, columns, scan);
        boolean startRowInclude = true, endRowInclude = false;
        if (null != rowInclude || rowInclude.length == 2) {
            startRowInclude = rowInclude[0];
            endRowInclude = rowInclude[1];
        }
        scan.withStartRow(Bytes.toBytes(startRow), startRowInclude);
        scan.withStopRow(Bytes.toBytes(stopRow), endRowInclude);
        if (ArrayUtil.isNotEmpty(filters)) {
            FilterList filterList = new FilterList(filters);
            scan.setFilter(filterList);
        }
        return scanFamilyMap(tableName, scan, family, columns);
    }

    /**
     * 指定列族、多个列，进行全表范围查询
     *
     * @param tableName 表名
     * @param family    列族
     * @param columns   将要返回的列（未指定的列不会返回）
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, String>> scanFamilyMap(TableName tableName, String family,
        Collection<String> columns) throws Exception {
        HBaseFamilyRequest request = new HBaseFamilyRequest();
        request.setFamily(family)
               .setColumns(columns)
               .setTableName(tableName.getNameAsString())
               .setReversed(true);
        return scanFamilyMap(request);
    }

    /**
     * 指定起止行、列族、多个列，进行范围查询
     *
     * @param startRow  起始行
     * @param stopRow   结束行
     * @param tableName 表名
     * @param family    列族
     * @param columns   将要返回的列（未指定的列不会返回）
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, String>> scanFamilyMap(TableName tableName, String startRow, String stopRow,
        String family, Collection<String> columns) throws Exception {
        HBaseFamilyRequest request = new HBaseFamilyRequest();
        request.setFamily(family)
               .setColumns(columns)
               .setTableName(tableName.getNameAsString())
               .setStartRow(startRow)
               .setStopRow(stopRow)
               .setReversed(true);
        return scanFamilyMap(request);
    }

    /**
     * 指定列族、多个列、{@link Filter}，进行全表范围查询
     *
     * @param tableName 表名
     * @param family    列族
     * @param columns   将要返回的列（未指定的列不会返回）
     * @param filter    {@link Filter} 实体
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, String>> scanFamilyMap(TableName tableName, String family,
        Collection<String> columns, Filter filter) throws Exception {
        HBaseFamilyRequest request = new HBaseFamilyRequest();
        request.setFamily(family)
               .setColumns(columns)
               .setTableName(tableName.getNameAsString())
               .setReversed(true)
               .addFilter(filter);
        return scanFamilyMap(request);
    }

    /**
     * 指定起止行、列族、多个列、{@link Filter}，进行范围查询
     *
     * @param startRow  起始行
     * @param stopRow   结束行
     * @param tableName 表名
     * @param family    列族
     * @param columns   将要返回的列（未指定的列不会返回）
     * @param filter    {@link Filter} 实体
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, String>> scanFamilyMap(TableName tableName, String startRow, String stopRow,
        String family, Collection<String> columns, Filter filter) throws Exception {
        HBaseFamilyRequest request = new HBaseFamilyRequest();
        request.setFamily(family)
               .setColumns(columns)
               .setTableName(tableName.getNameAsString())
               .setStartRow(startRow)
               .setStopRow(stopRow)
               .setReversed(true)
               .addFilter(filter);
        return scanFamilyMap(request);
    }

    /**
     * 指定起止写入时间、列族、多个列、{@link Filter}，进行范围查询
     * <p>
     * 注：根据时间范围查询时，会强制按时序倒序排列
     *
     * @param tableName 表名
     * @param family    列族
     * @param columns   将要返回的列（未指定的列不会返回）
     * @param minStamp  起始写入时间
     * @param maxStamp  结束写入时间
     * @param filter    {@link Filter} 实体
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, String>> scanFamilyMap(TableName tableName, String family,
        Collection<String> columns, long minStamp, long maxStamp, Filter filter) throws Exception {
        HBaseFamilyRequest request = new HBaseFamilyRequest();
        request.setFamily(family)
               .setColumns(columns)
               .setTableName(tableName.getNameAsString())
               .setMinTimeStamp(minStamp)
               .setMaxTimeStamp(maxStamp)
               .setReversed(true)
               .addFilter(filter);
        return scanFamilyMap(request);
    }

    /**
     * 返回匹配请求条件的数据、{@link Filter}，进行范围查询
     * <p>
     * 注：根据时间范围查询时，会强制按时序倒序排列
     *
     * @param request {@link HBaseFamilyRequest} 请求条件
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, String>> scanFamilyMap(HBaseFamilyRequest request) throws Exception {
        return scanFamilyMap(TableName.valueOf(request.getTableName()),
            request.toScan(), request.getFamily(), request.getColumns());
    }

    /**
     * 指定列族、多个列、{@link Scan}，进行范围查询
     *
     * @param tableName 表名
     * @param scan      {@link Scan} 实体
     * @param family    列族
     * @param columns   将要返回的列（未指定的列不会返回）
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, String>> scanFamilyMap(TableName tableName, Scan scan,
        String family, Collection<String> columns) throws Exception {

        Table table = getTable(tableName);
        ResultScanner scanner = null;
        try {
            scanner = table.getScanner(scan);
            Map<String, Map<String, String>> map = new LinkedHashMap<>();
            for (Result result : scanner) {
                Map<String, String> columnMap = new HashMap<>(columns.size());
                for (String column : columns) {
                    String value = Bytes.toString(result.getValue(Bytes.toBytes(family), Bytes.toBytes(column)));
                    columnMap.put(column, value);
                }
                map.put(Bytes.toString(result.getRow()), columnMap);
            }
            return map;
        } finally {
            IoUtil.close(scanner);
            recycle(table);
        }
    }

    private static void fillColumnsToScan(String family, Collection<String> columns, Scan scan) {
        if (StrUtil.isNotBlank(family) && CollectionUtil.isNotEmpty(columns)) {
            for (String column : columns) {
                scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            }
        }
    }

    /**
     * 指定多个列族，每个列族包含的列、{@link Scan}，进行范围查询
     *
     * @param tableName     表名
     * @param scan          {@link Scan} 实体
     * @param familyColumns 列族, 列族所包含的列
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列族；三级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, Map<String, String>>> scanMultiFamilyMap(TableName tableName, Scan scan,
        Map<String, Collection<String>> familyColumns) throws Exception {

        if (MapUtil.isEmpty(familyColumns)) {
            return scanMultiFamilyMap(tableName, scan);
        }

        Table table = getTable(tableName);
        ResultScanner scanner = null;
        try {
            scanner = table.getScanner(scan);
            Map<String, Map<String, Map<String, String>>> familyKvDataMap = new LinkedHashMap<>();
            for (Result result : scanner) {
                Map<String, Map<String, String>> familyMap = new HashMap<>();
                familyColumns.forEach((family, columns) -> {
                    Map<String, String> columnMap = new HashMap<>();
                    if (CollectionUtil.isNotEmpty(columns)) {
                        for (String column : columns) {
                            String value = Bytes.toString(result.getValue(Bytes.toBytes(family),
                                Bytes.toBytes(column)));
                            columnMap.put(column, value);
                        }
                    }
                    familyMap.put(family, columnMap);
                });
                familyKvDataMap.put(Bytes.toString(result.getRow()), familyMap);
            }
            return familyKvDataMap;
        } finally {
            IoUtil.close(scanner);
            recycle(table);
        }
    }

    /**
     * 返回匹配 {@link Scan} 的所有列族的数据
     *
     * @param tableName 表名
     * @param scan      {@link Scan} 实体
     * @return 一级 Map 的 key 是 Row Key；二级 Map 的 key 是列族；三级 Map 的 key 是列，value 是列值
     */
    public Map<String, Map<String, Map<String, String>>> scanMultiFamilyMap(TableName tableName, Scan scan)
        throws Exception {
        Table table = getTable(tableName);
        ResultScanner scanner = null;
        try {
            scanner = table.getScanner(scan);
            Result[] results = ArrayUtil.toArray(scanner, Result.class);
            return getAllFamilyMapInRows(results);
        } finally {
            IoUtil.close(scanner);
            recycle(table);
        }
    }

    public Map<String, Map<String, Map<String, String>>> scanMultiFamilyMap(HBaseMultiFamilyRequest request)
        throws Exception {
        return scanMultiFamilyMap(TableName.valueOf(request.getTableName()), request.toScan(),
            request.getFamilyColumns());
    }

    private Map<String, Map<String, Map<String, String>>> getAllFamilyMapInRows(Result[] results) {
        Map<String, Map<String, Map<String, String>>> rowFamilyColumnMap = new HashMap<>();
        for (Result result : results) {
            if (result == null || result.isEmpty()) {
                continue;
            }
            Map<String, Map<String, String>> familyColumnMap = new HashMap<>();
            for (Cell cell : result.listCells()) {
                String family = Bytes.toString(CellUtil.cloneFamily(cell));
                if (!familyColumnMap.containsKey(family)) {
                    familyColumnMap.put(family, new HashMap<>());
                }
                String column = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                familyColumnMap.get(family).put(column, value);
            }
            rowFamilyColumnMap.put(Bytes.toString(result.getRow()), familyColumnMap);
        }
        return rowFamilyColumnMap;
    }

    /**
     * 扫描（scan）一个列族的数据，并返回列表记录
     *
     * @param request 单列族请求
     * @return /
     */
    public List<HBaseRowData> listRowData(HBaseFamilyRequest request) throws Exception {
        Map<String, Map<String, String>> rowColumnMap = scanFamilyMap(request);
        return HBaseRowData.toRowList(request.getFamily(), rowColumnMap);
    }

    /**
     * 扫描（scan）多个列族的数据，并返回列表记录
     *
     * @param request 多列族请求
     * @return /
     */
    public List<HBaseRowData> listRowData(HBaseMultiFamilyRequest request) throws Exception {
        Map<String, Map<String, Map<String, String>>> map = scanMultiFamilyMap(request);
        return HBaseRowData.toRowList(map);
    }

    public PageData<HBaseRowData> pageRowData(HBaseMultiFamilyRequest request) throws Exception {
        return pageRowData(TableName.valueOf(request.getTableName()), request.getFamilyColumns(),
            request.getPageNo(), request.getPageSize(), request.toScan());
    }

    public PageData<HBaseRowData> pageRowData(TableName tableName, Map<String, Collection<String>> familyColumns,
        Integer pageNo, Integer pageSize, Scan scan) throws Exception {

        Table table = getTable(tableName);
        Map<String, Map<String, Map<String, String>>> rowMap = new HashMap<>();

        int page = 1;
        byte[] lastRow = null;
        long total = 0L;
        while (true) {
            if (lastRow != null) {
                scan.withStartRow(lastRow, false);
            }
            ResultScanner rs = table.getScanner(scan);
            Iterator<Result> it = rs.iterator();
            int count = 0;
            while (it.hasNext()) {
                Result result = it.next();
                if (pageNo == page) {
                    fillRowMap(result, familyColumns, rowMap);
                }
                lastRow = result.getRow();
                count++;
            }

            page++;
            rs.close();
            total += count;
            if (count == 0) {
                break;
            }
        }
        recycle(table);
        List<HBaseRowData> content = HBaseRowData.toRowList(rowMap);
        return new PageData<>(pageNo, pageSize, total, content);
    }

    private void fillRowMap(Result result, Map<String, Collection<String>> familyColumns,
        Map<String, Map<String, Map<String, String>>> rowMap) {

        String row = Bytes.toString(result.getRow());
        if (row == null) {
            return;
        }

        Map<String, Map<String, String>> familyMap;
        if (MapUtil.isEmpty(familyColumns)) {
            familyMap = new HashMap<>();
            for (Cell cell : result.listCells()) {
                String family = Bytes.toString(CellUtil.cloneFamily(cell));
                if (!familyMap.containsKey(family)) {
                    familyMap.put(family, new HashMap<>());
                }
                String column = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                familyMap.get(family).put(column, value);
            }
        } else {
            familyMap = new HashMap<>(familyColumns.size());
            familyColumns.forEach((family, columns) -> {
                if (CollectionUtil.isNotEmpty(columns)) {
                    Map<String, String> columnMap = new HashMap<>(columns.size());
                    for (String column : columns) {
                        String value = Bytes.toString(result.getValue(Bytes.toBytes(family), Bytes.toBytes(column)));
                        columnMap.put(column, value);
                    }
                    familyMap.put(family, columnMap);
                }
            });
        }
        rowMap.put(row, familyMap);
    }

    public void dumpResult(Result result) {
        for (Cell cell : result.rawCells()) {
            String msg = StrUtil.format("Cell: {}, Value: {}", cell,
                Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            System.out.println(msg);
        }
    }

    private void recycle(Table table) {
        if (null == table) {
            return;
        }
        IoUtil.close(table);
    }

}
