package io.github.dunwu.javadb.hbase.constant;

import lombok.Getter;

/**
 * 生成ID类型枚举类
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-17
 */
@Getter
public enum RowType {

    /**
     * 原 ID
     */
    ORIGIN_ID(1),

    /**
     * 以 10 位的时间戳（秒级）作为 ID
     * <p>
     * 特点：数据存储保证单调递增，适用于 scan 为主，且数据量不大（100w以内），读频率不高的业务场景。
     */
    TIMESTAMP(2),

    /**
     * UUID作为主键，适合数据量较大，且以 get 为主的场景（尽量保证数据存储离散）
     */
    UUID(3),

    /**
     * ID = bucket(2/3) + timestamp(10) + bizId，适合数据量较大，且需要大量 scan 的场景
     * <p>
     * 注：如果选择此 ID 类型，必须在 @TableId 中指定分桶数
     */
    BUCKET(4);

    private final int key;

    RowType(int key) {
        this.key = key;
    }
}