package io.github.dunwu.javadb.hbase.mapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 通用 Mapper
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-22
 */
public interface CommonMapper<T> {

    /**
     * 插入一条记录
     *
     * @param entity 实体对象
     */
    int insert(T entity);

    /**
     * 批量插入记录
     *
     * @param list 实体对象列表
     */
    int insertBatch(Collection<T> list);

    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    int deleteById(Serializable id);

    /**
     * 根据实体(ID)删除
     *
     * @param entity 实体对象
     */
    int deleteById(T entity);

    /**
     * 删除（根据ID或实体 批量删除）
     *
     * @param idList 主键ID列表或实体列表(不能为 null 以及 empty)
     */
    int deleteBatchIds(Collection<? extends Serializable> idList);

    /**
     * 根据 ID 修改
     *
     * @param entity 实体对象
     */
    int updateById(T entity);

    /**
     * 批量更新记录
     *
     * @param list 实体对象列表
     */
    int updateBatchById(Collection<T> list);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    T getOneById(Serializable id);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    List<T> getListByIds(Collection<? extends Serializable> idList);

}
