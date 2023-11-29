package io.github.dunwu.javadb.hbase.mapper;

import io.github.dunwu.javadb.hbase.HbaseTemplate;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseContentEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * HBase Mapper 基础类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
@Slf4j
public abstract class BaseHbaseUkMapper<T extends BaseHbaseContentEntity> extends BaseHbaseMapper<T>
    implements HbaseUkMapper<T> {

    public BaseHbaseUkMapper(HbaseTemplate hbaseTemplate) {
        super(hbaseTemplate);
    }

}
