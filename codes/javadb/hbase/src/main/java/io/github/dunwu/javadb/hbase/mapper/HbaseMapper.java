package io.github.dunwu.javadb.hbase.mapper;

import cn.hutool.core.map.MapUtil;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;
import org.apache.hadoop.hbase.client.Connection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Hbase Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public interface HbaseMapper<T extends BaseHbaseEntity> extends CommonMapper<T> {

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
     * 获取列族
     */
    String getFamily();

    /**
     * 获取实体类型
     */
    Class<T> getEntityClass();

    @Override
    default int insert(T entity) {
        return save(entity);
    }

    @Override
    default int updateById(T entity) {
        return save(entity);
    }

    @Override
    default int insertBatch(Collection<T> list) {
        return batchSave(list);
    }

    @Override
    default int updateBatchById(Collection<T> list) {
        return batchSave(list);
    }

    /**
     * 保存一条记录
     *
     * @param entity 实体对象
     */
    int save(T entity);

    /**
     * 批量保存记录
     *
     * @param list 实体对象列表
     */
    int batchSave(Collection<T> list);

    @Override
    default List<T> getListByIds(Collection<? extends Serializable> ids) {
        Map<? extends Serializable, T> map = getMapByIds(ids);
        if (MapUtil.isEmpty(map)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(map.values());
    }

    /**
     * 根据 ID 列表批量查数据，以 Map 形式返回
     *
     * @param ids 即 Hbase rowkey
     * @return /
     */
    Map<? extends Serializable, T> getMapByIds(Collection<? extends Serializable> ids);

    /**
     * 根据 ID 滚动分页查询
     *
     * @param scrollId 为空值时，默认查第一页
     * @param size     每页记录数
     * @return /
     */
    List<T> scroll(Serializable scrollId, int size);

}
