package io.github.dunwu.javadb.hbase;

import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;
import org.apache.hadoop.hbase.client.Connection;

import java.util.Collection;
import java.util.List;

/**
 * Hbase Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public interface HbaseMapper<T extends BaseHbaseEntity> {

    /**
     * 获取 Hbase 官方客户端实体
     */
    Connection getClient();

    /**
     * 获取命名空间
     */
    String getNamespace();

    /**
     * 获取表名
     */
    String getTableName();

    /**
     * 判断表是否存在
     */
    boolean existsTable();

    /**
     * 获取列族
     */
    String getFamily();

    /**
     * 获取实体类型
     */
    Class<T> getEntityClass();

    /**
     * 根据 ID 查数据
     *
     * @param id 即 Hbase rowkey
     * @return /
     */
    T pojoById(String id);

    /**
     * 根据 ID 列表批量查数据
     *
     * @param ids 即 Hbase rowkey
     * @return /
     */
    List<T> pojoListByIds(Collection<String> ids);

    /**
     * 根据 ID 滚动分页查询
     *
     * @param scrollId 为空值时，默认查第一页
     * @param size     每页记录数
     * @return /
     */
    List<T> scroll(String scrollId, int size);

    /**
     * 保存实体
     *
     * @param entity 实体（存于默认的 Hbase 列族 f）
     * @return /
     */
    T save(T entity);

    /**
     * 保存实体列表，每条记录将作为一行保存
     *
     * @param list 实体列表（存于默认的 Hbase 列族 f）
     * @return /
     */
    boolean batchSave(Collection<T> list);

    /**
     * 根据 ID 删除记录
     *
     * @param id 即 Hbase rowkey
     * @return /
     */
    boolean deleteById(String id);

    /**
     * 根据 ID 列表批量删除记录
     *
     * @param ids 即 Hbase rowkey
     * @return /
     */
    boolean batchDeleteById(Collection<String> ids);

}
