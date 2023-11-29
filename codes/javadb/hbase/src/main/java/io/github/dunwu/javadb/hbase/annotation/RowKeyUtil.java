package io.github.dunwu.javadb.hbase.annotation;

import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.dunwu.javadb.hbase.constant.RowType;
import io.github.dunwu.javadb.hbase.entity.BaseHbaseEntity;

import java.lang.reflect.Method;

/**
 * {@link RowKeyRule} 解析器
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-20
 */
public class RowKeyUtil {

    /**
     * 获取主键
     */
    public static <T extends BaseHbaseEntity> String getRowKey(T entity) throws IllegalArgumentException {

        String row = null;
        Class<? extends BaseHbaseEntity> clazz = entity.getClass();
        RowKeyRule rule = getRowKeyRule(entity.getClass());
        Method method = ReflectUtil.getMethodByName(clazz, rule.uk());
        if (method == null) {
            String msg = StrUtil.format("{} 实体类定义错误！@RowKeyRule 指定的 uk：{} 方法未找到！",
                clazz.getCanonicalName(), rule.uk());
            throw new IllegalArgumentException(msg);
        }
        switch (rule.type()) {
            case ORIGIN_ID:
                row = getRowKeyForOriginId(entity, method, rule.length());
                break;
            case TIMESTAMP:
                row = getRowKeyForTimestamp();
                break;
            case UUID:
                row = IdUtil.fastSimpleUUID();
                break;
            case BUCKET:
                row = getRowKeyForBucket(entity, method, rule.length(), rule.bucket());
            default:
                break;
        }

        if (StrUtil.isBlank(row)) {
            throw new IllegalArgumentException(StrUtil.format("实体定义错误！未定义 @RowKeyRule", entity.getClass(),
                BaseHbaseEntity.class.getCanonicalName()));
        }
        return row;
    }

    public static RowKeyRule getRowKeyRule(Class<? extends BaseHbaseEntity> clazz) {

        RowKeyRule rule = clazz.getAnnotation(RowKeyRule.class);

        if (rule == null) {
            String msg = StrUtil.format("{} 实体类定义错误！未定义 @RowKeyRule", clazz.getCanonicalName());
            throw new IllegalArgumentException(msg);
        }

        if (rule.type() == RowType.ORIGIN_ID && rule.length() <= 0) {
            String msg = StrUtil.format("{} 实体类定义错误！@RowKeyRule type 为 ORIGIN_ID 时，length 必须大于 0！",
                clazz.getCanonicalName());
            throw new IllegalArgumentException(msg);
        }

        if (rule.type() == RowType.BUCKET && (rule.length() <= 0 || rule.bucket() <= 0)) {
            String msg = StrUtil.format("{} 实体类定义错误！@RowKeyRule type 为 BUCKET 时，length 和 bucket 必须大于 0！",
                clazz.getCanonicalName());
            throw new IllegalArgumentException(msg);
        }
        return rule;
    }

    public static <T extends BaseHbaseEntity> String getRowKeyForOriginId(T entity, Method method, int length)
        throws IllegalArgumentException {
        String originId;
        Object value = ReflectUtil.invoke(entity, method);
        if (value instanceof String) {
            originId = (String) value;
        } else {
            originId = String.valueOf(value);
        }
        if (length == 0) {
            throw new IllegalArgumentException("length 不能为 0");
        }
        return getRowKeyForOriginId(originId, length);
    }

    public static String getRowKeyForOriginId(String bizId, int length) {
        return StrUtil.padPre(bizId, length, "0");
    }

    public static String getRowKeyForTimestamp() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        return StrUtil.padPre(timestamp, 10, "0");
    }

    public static <T extends BaseHbaseEntity> String getRowKeyForBucket(T entity, Method method, int length, int bucket)
        throws IllegalArgumentException {
        if (bucket == 0) {
            throw new IllegalArgumentException("bucket 不能为 0");
        }

        String originId = getRowKeyForOriginId(entity, method, length);
        int bucketLength = getBucketIdLength(bucket);
        String bucketId = String.valueOf(HashUtil.fnvHash(originId) % bucket);
        return StrUtil.padPre(bucketId, bucketLength, "0") + originId;
    }

    public static <T extends BaseHbaseEntity> String getRowKeyForBucket(String contentId, Class<T> clazz) {
        RowKeyRule rule = RowKeyUtil.getRowKeyRule(clazz);
        return RowKeyUtil.getRowKeyForBucket(contentId, rule.length(), rule.bucket());
    }

    public static String getRowKeyForBucket(String bizId, int length, int bucket) throws IllegalArgumentException {
        String originId = getRowKeyForOriginId(bizId, length);
        int bucketLength = getBucketIdLength(bucket);
        String bucketId = String.valueOf(HashUtil.fnvHash(originId) % bucket);
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
