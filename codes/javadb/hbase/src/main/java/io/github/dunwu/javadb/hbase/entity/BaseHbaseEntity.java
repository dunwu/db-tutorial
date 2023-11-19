package io.github.dunwu.javadb.hbase.entity;

import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.dunwu.javadb.hbase.annotation.RowKeyRule;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * HBase 基础实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-15
 */
public abstract class BaseHbaseEntity implements Serializable {

    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    protected String rowKey;

    /**
     * 获取主键
     */
    public String getRowKey() throws IOException {
        if (StrUtil.isNotBlank(rowKey)) {
            return rowKey;
        }

        String row = null;
        Field[] fields = ReflectUtil.getFields(this.getClass());
        for (Field field : fields) {
            RowKeyRule rule = field.getAnnotation(RowKeyRule.class);
            if (rule != null) {
                switch (rule.type()) {
                    case ORIGIN_ID:
                        row = getRowKeyForOriginId(this, field, rule);
                        break;
                    case TIMESTAMP:
                        row = getRowKeyForTimestamp();
                        break;
                    case UUID:
                        row = IdUtil.fastSimpleUUID();
                        break;
                    case BUCKET:
                        row = getRowKeyForBucket(this, field, rule);
                    default:
                        break;
                }
            }
        }

        if (StrUtil.isBlank(row)) {
            throw new IOException(StrUtil.format("实体定义错误！未定义 @RowKeyRule",
                this.getClass(), BaseHbaseEntity.class.getCanonicalName()));
        }
        this.rowKey = row;
        return row;
    }

    private static <T extends BaseHbaseEntity> String getRowKeyForOriginId(T entity, Field field, RowKeyRule rowKeyRule)
        throws IOException {
        String originId;
        Object value = ReflectUtil.getFieldValue(entity, field);
        if (value instanceof String) {
            originId = (String) value;
        } else {
            originId = String.valueOf(value);
        }
        if (rowKeyRule.length() == 0) {
            throw new IOException(
                StrUtil.format("实体定义错误！{} 选择 @RowKey 的 type 为 {}，必须指定 length 且不能为 0",
                    entity.getClass(), rowKeyRule.type()));
        }
        return StrUtil.padPre(originId, rowKeyRule.length(), "0");
    }

    private static String getRowKeyForTimestamp() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        return StrUtil.padPre(timestamp, 10, "0");
    }

    private static <T extends BaseHbaseEntity> String getRowKeyForBucket(T entity, Field field, RowKeyRule rowKeyRule)
        throws IOException {
        if (rowKeyRule.bucket() == 0) {
            throw new IOException(
                StrUtil.format("实体定义错误！{} 选择 @RowKey 的 type 为 {}，必须指定 bucket 且不能为 0",
                    entity.getClass(), rowKeyRule.type()));
        }

        String originId = getRowKeyForOriginId(entity, field, rowKeyRule);
        int bucketLength = getBucketIdLength(rowKeyRule.bucket());
        String bucketId = String.valueOf(HashUtil.fnvHash(originId) % rowKeyRule.bucket());
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        return StrUtil.padPre(bucketId, bucketLength, "0")
            + StrUtil.padPre(timestamp, 10, "0")
            + originId;
    }

    private static int getBucketIdLength(int bucket) {
        bucket = bucket - 1;
        if (bucket <= 0) {
            return 1;
        }

        int length = 0;
        while (bucket > 0) {
            length++;
            bucket = bucket / 10;
        }
        return length;
    }

    private static final long serialVersionUID = 5075127328254616085L;

}
