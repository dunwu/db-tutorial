package io.github.dunwu.javadb.hbase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;
import io.github.dunwu.javadb.hbase.entity.ScrollData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * HBase Mapper 基础类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseHbaseMapper<T extends BaseHbaseEntity> implements HbaseMapper<T> {

    protected final HbaseTemplate hbaseTemplate;

    protected final HbaseAdmin hbaseAdmin;

    @Override
    public Connection getClient() {
        return hbaseTemplate.getConnection();
    }

    @Override
    public String getNamespace() {
        return "test";
    }

    @Override
    public boolean existsTable() {
        byte[] namespace = Bytes.toBytes(getNamespace());
        byte[] tableName = Bytes.toBytes(getTableName());
        try {
            return hbaseAdmin.existsTable(TableName.valueOf(namespace, tableName));
        } catch (IOException e) {
            log.error("【Hbase】existsTable 异常", e);
            return false;
        }
    }

    @Override
    public String getFamily() {
        return "f";
    }

    @Override
    public T pojoByRowKey(String rowKey) {
        if (StrUtil.isBlank(rowKey)) {
            return null;
        }
        try {
            return hbaseTemplate.getEntity(getFullTableName(), rowKey, getFamily(), getEntityClass());
        } catch (IOException e) {
            log.error("【Hbase】pojoById 异常", e);
            return null;
        }
    }

    @Override
    public List<T> pojoListByRowKeys(Collection<String> rowKeys) {
        if (CollectionUtil.isEmpty(rowKeys)) {
            return null;
        }
        try {
            return hbaseTemplate.getEntityList(getFullTableName(), rowKeys.toArray(new String[0]),
                getFamily(), getEntityClass());
        } catch (IOException e) {
            log.error("【Hbase】getEntityList 异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<T> scroll(String scrollRowKey, int size) {
        try {
            ScrollData<T> scrollData =
                hbaseTemplate.getEntityScroll(getFullTableName(), getFamily(), scrollRowKey, size, getEntityClass());
            if (scrollData == null || CollectionUtil.isEmpty(scrollData.getContent())) {
                return new ArrayList<>();
            }
            return new ArrayList<>(scrollData.getContent());
        } catch (IOException e) {
            log.error("【Hbase】getEntityScroll 异常", e);
            return new ArrayList<>();
        }
    }

    @Override
    public T save(T entity) {
        try {
            String rowKey = entity.getRowKey();
            hbaseTemplate.put(getFullTableName(), rowKey, getFamily(), entity);
            return entity;
        } catch (IOException e) {
            log.error("【Hbase】put 异常", e);
            return null;
        }
    }

    @Override
    public boolean batchSave(Collection<T> list) {
        try {
            hbaseTemplate.batchPut(getFullTableName(), getFamily(), list);
            return true;
        } catch (IOException | InterruptedException e) {
            log.error("【Hbase】batchPut 异常", e);
            return false;
        }
    }

    @Override
    public boolean delete(String rowKey) {
        if (StrUtil.isBlank(rowKey)) {
            return true;
        }
        try {
            hbaseTemplate.delete(getFullTableName(), rowKey);
            return true;
        } catch (IOException e) {
            log.error("【Hbase】delete 异常", e);
            return false;
        }
    }

    @Override
    public boolean batchDelete(Collection<String> rowKeys) {
        if (CollectionUtil.isEmpty(rowKeys)) {
            return true;
        }
        try {
            hbaseTemplate.batchDelete(getFullTableName(), rowKeys.toArray(new String[0]));
            return true;
        } catch (IOException | InterruptedException e) {
            log.error("【Hbase】batchDelete 异常", e);
            return false;
        }
    }

    protected String getFullTableName() {
        return StrUtil.format("{}:{}", getNamespace(), getTableName());
    }

}
