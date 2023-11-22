package io.github.dunwu.javadb.hbase.annotation;

import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;

import java.lang.reflect.Field;

/**
 * {@link RowKeyRule} 解析器
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-20
 */
public class RowKeyRuleParser {

    /**
     * 获取主键
     */
    public static <T extends BaseHbaseEntity> String getRowKey(T entity) throws IllegalArgumentException {

        String row = null;
        Class<? extends BaseHbaseEntity> clazz = entity.getClass();
        RowKeyRule rule = clazz.getAnnotation(RowKeyRule.class);
        if (rule == null) {
            throw new IllegalArgumentException(StrUtil.format("实体定义错误！未定义 @RowKeyRule", entity.getClass(),
                BaseHbaseEntity.class.getCanonicalName()));
        }

        Field field = ReflectUtil.getField(clazz, rule.pk());
        if (field == null) {
            throw new IllegalArgumentException(StrUtil.format("实体定义错误！@RowKeyRule 中未指定 value", entity.getClass(),
                BaseHbaseEntity.class.getCanonicalName()));
        }

        switch (rule.type()) {
            case ORIGIN_ID:
                row = getRowKeyForOriginId(entity, field, rule);
                break;
            case TIMESTAMP:
                row = getRowKeyForTimestamp();
                break;
            case UUID:
                row = IdUtil.fastSimpleUUID();
                break;
            case BUCKET:
                row = getRowKeyForBucket(entity, field, rule);
            default:
                break;
        }

        if (StrUtil.isBlank(row)) {
            throw new IllegalArgumentException(StrUtil.format("实体定义错误！未定义 @RowKeyRule", entity.getClass(),
                BaseHbaseEntity.class.getCanonicalName()));
        }
        return row;
    }

    private static <T extends BaseHbaseEntity> String getRowKeyForOriginId(T entity, Field field, RowKeyRule rule)
        throws IllegalArgumentException {
        String originId;
        Object value = ReflectUtil.getFieldValue(entity, field);
        if (value instanceof String) {
            originId = (String) value;
        } else {
            originId = String.valueOf(value);
        }
        if (rule.length() == 0) {
            throw new IllegalArgumentException(
                StrUtil.format("实体定义错误！{} 选择 @RowKey 的 type 为 {}，必须指定 length 且不能为 0",
                    entity.getClass(), rule.type()));
        }
        return StrUtil.padPre(originId, rule.length(), "0");
    }

    private static String getRowKeyForTimestamp() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        return StrUtil.padPre(timestamp, 10, "0");
    }

    private static <T extends BaseHbaseEntity> String getRowKeyForBucket(T entity, Field field, RowKeyRule rowKeyRule)
        throws IllegalArgumentException {
        if (rowKeyRule.bucket() == 0) {
            throw new IllegalArgumentException(
                StrUtil.format("实体定义错误！{} 选择 @RowKey 的 type 为 {}，必须指定 bucket 且不能为 0",
                    entity.getClass(), rowKeyRule.type()));
        }

        String originId = getRowKeyForOriginId(entity, field, rowKeyRule);
        int bucketLength = getBucketIdLength(rowKeyRule.bucket());
        String bucketId = String.valueOf(HashUtil.fnvHash(originId) % rowKeyRule.bucket());
        return StrUtil.padPre(bucketId, bucketLength, "0") + originId;
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

}
